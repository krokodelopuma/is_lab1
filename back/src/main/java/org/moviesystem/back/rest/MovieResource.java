package org.moviesystem.back.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.moviesystem.back.model.Movie;
import org.moviesystem.back.service.MovieService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API ресурс для работы с фильмами.
 * 
 * Этот класс предоставляет HTTP API для всех операций с фильмами:
 * 
 * Основные операции:
 * - GET /movies - получение списка фильмов с фильтрацией и пагинацией
 * - POST /movies - создание нового фильма
 * - GET /movies/{id} - получение фильма по ID
 * - PUT /movies/{id} - обновление фильма
 * - DELETE /movies/{id} - удаление фильма
 * 
 * Специальные операции:
 * - GET /movies/sum-golden-palm - сумма Золотых пальм
 * - POST /movies/redistribute-oscars - перераспределение Оскаров
 * - GET /movies/by-golden-palm/{count} - фильмы с определенным количеством Золотых пальм
 * - GET /movies/genres - список всех жанров
 * - GET /movies/oscars-summary - сводка по Оскарам
 * 
 * Все методы возвращают JSON и поддерживают CORS для работы с frontend.
 * 
 * @author Movie Management System Team
 * @version 1.0
 */

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {
    
    
    @Inject
    private MovieService movieService;
    
    @GET
    public Response getAllMovies(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("search") String search,
            @QueryParam("genre") String genre,
            @QueryParam("director") String director,
            @QueryParam("minOscars") Integer minOscars,
            @QueryParam("maxOscars") Integer maxOscars,
            @QueryParam("sort") @DefaultValue("id") String sortField,
            @QueryParam("order") @DefaultValue("asc") String sortOrder) {
        try {
            List<Movie> movies;
            long totalCount;
            
            // Комбинированная фильтрация - все фильтры работают вместе
            if (search != null && !search.trim().isEmpty()) {
                // Если есть поиск, используем его как основной фильтр
                movies = movieService.searchMoviesByName(search, sortField, sortOrder);
                totalCount = movies.size();
                
                // Дополнительно применяем другие фильтры
                if (genre != null && !genre.trim().isEmpty()) {
                    movies = movies.stream()
                            .filter(movie -> movie.getGenre().toString().equals(genre))
                            .collect(Collectors.toList());
                }
                if (director != null && !director.trim().isEmpty()) {
                    movies = movies.stream()
                            .filter(movie -> movie.getDirector() != null && 
                                    movie.getDirector().getName().toLowerCase().contains(director.toLowerCase()))
                            .collect(Collectors.toList());
                }
                if (minOscars != null || maxOscars != null) {
                    int min = minOscars != null ? minOscars : 0;
                    int max = maxOscars != null ? maxOscars : Integer.MAX_VALUE;
                    movies = movies.stream()
                            .filter(movie -> movie.getOscarsCount() >= min && movie.getOscarsCount() <= max)
                            .collect(Collectors.toList());
                }
            } else {
                // Если нет поиска, используем отдельные фильтры
                if (genre != null && !genre.trim().isEmpty()) {
                    movies = movieService.getMoviesByGenre(genre, sortField, sortOrder);
                    totalCount = movies.size();
                } else if (director != null && !director.trim().isEmpty()) {
                    movies = movieService.getMoviesByDirector(director, sortField, sortOrder);
                    totalCount = movies.size();
                } else if (minOscars != null || maxOscars != null) {
                    int min = minOscars != null ? minOscars : 0;
                    int max = maxOscars != null ? maxOscars : Integer.MAX_VALUE;
                    movies = movieService.getMoviesByOscarsRange(min, max, sortField, sortOrder);
                    totalCount = movies.size();
                } else {
                    movies = movieService.getMovies(page, size, sortField, sortOrder);
                    totalCount = movieService.getMoviesCount();
                }
            }
            
            return Response.ok()
                    .entity(new MovieResponse(movies, totalCount, page, size))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving movies: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getMovieById(@PathParam("id") Long id) {
        try {
            Optional<Movie> movie = movieService.getMovieById(id);
            if (movie.isPresent()) {
                return Response.ok(movie.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Movie not found"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving movie: " + e.getMessage()))
                    .build();
        }
    }
    
    @POST
    @Transactional
    public Response createMovie(Movie movie) {
        try {
            Movie createdMovie = movieService.createMovie(movie);
            return Response.status(Response.Status.CREATED)
                    .entity(createdMovie)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error creating movie: " + e.getMessage()))
                    .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateMovie(@PathParam("id") Long id, Movie movie) {
        try {
            movie.setId(id);
            Movie updatedMovie = movieService.updateMovie(movie);
            return Response.ok(updatedMovie).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error updating movie: " + e.getMessage()))
                    .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteMovie(@PathParam("id") Long id) {
        try {
            movieService.deleteMovie(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error deleting movie: " + e.getMessage()))
                    .build();
        }
    }
    
    // Специальные операции
    @DELETE
    @Path("/by-golden-palm/{goldenPalmCount}")
    @Transactional
    public Response deleteMoviesByGoldenPalmCount(@PathParam("goldenPalmCount") Long goldenPalmCount) {
        try {
            movieService.deleteMoviesByGoldenPalmCount(goldenPalmCount);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error deleting movies: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/sum-golden-palm")
    public Response getSumGoldenPalmCount() {
        try {
            Long sum = movieService.getSumGoldenPalmCount();
            return Response.ok(new SumResponse(sum)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error calculating sum: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/by-golden-palm-greater/{goldenPalmCount}")
    public Response getMoviesByGoldenPalmCountGreaterThan(@PathParam("goldenPalmCount") Long goldenPalmCount) {
        try {
            List<Movie> movies = movieService.getMoviesByGoldenPalmCountGreaterThan(goldenPalmCount);
            return Response.ok(movies).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving movies: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/by-golden-palm/{goldenPalmCount}")
    public Response getMoviesByGoldenPalmCount(@PathParam("goldenPalmCount") Long goldenPalmCount) {
        try {
            List<Movie> movies = movieService.getMoviesByGoldenPalmCount(goldenPalmCount);
            return Response.ok(movies).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving movies: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/oscars-summary")
    public Response getOscarsSummary() {
        try {
            List<Movie> allMovies = movieService.getAllMovies();
            Map<String, Integer> genreOscars = new HashMap<>();
            int totalOscars = 0;
            
            for (Movie movie : allMovies) {
                String genre = movie.getGenre().toString();
                int oscars = movie.getOscarsCount();
                genreOscars.put(genre, genreOscars.getOrDefault(genre, 0) + oscars);
                totalOscars += oscars;
            }
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("genreOscars", genreOscars);
            summary.put("totalOscars", totalOscars);
            summary.put("totalMovies", allMovies.size());
            
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving oscars summary: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/genres")
    public Response getAllGenres() {
        try {
            List<String> genres = movieService.getAllGenres();
            return Response.ok(genres).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving genres: " + e.getMessage()))
                    .build();
        }
    }
    
    @POST
    @Path("/redistribute-oscars")
    @Transactional
    public Response redistributeOscars(@QueryParam("fromGenre") String fromGenre, 
                                     @QueryParam("toGenre") String toGenre) {
        try {
            movieService.redistributeOscars(fromGenre, toGenre);
            return Response.ok(new MessageResponse("Oscars redistributed successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error redistributing oscars: " + e.getMessage()))
                    .build();
        }
    }
    
    @POST
    @Path("/add-oscars-to-long-movies")
    @Transactional
    public Response addOscarsToLongMovies(@QueryParam("minLength") Long minLength,
                                        @QueryParam("oscarsToAdd") int oscarsToAdd) {
        try {
            movieService.addOscarsToLongMovies(minLength, oscarsToAdd);
            return Response.ok(new MessageResponse("Oscars added successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error adding oscars: " + e.getMessage()))
                    .build();
        }
    }
    
    // Вспомогательные классы для ответов
    public static class MovieResponse {
        public List<Movie> movies;
        public long totalCount;
        public int page;
        public int size;
        
        public MovieResponse() {}
        
        public MovieResponse(List<Movie> movies, long totalCount, int page, int size) {
            this.movies = movies;
            this.totalCount = totalCount;
            this.page = page;
            this.size = size;
        }
    }
    
    public static class ErrorResponse {
        public String error;
        
        public ErrorResponse() {}
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
    
    public static class SumResponse {
        public Long sum;
        
        public SumResponse() {}
        
        public SumResponse(Long sum) {
            this.sum = sum;
        }
    }
    
    public static class MessageResponse {
        public String message;
        
        public MessageResponse() {}
        
        public MessageResponse(String message) {
            this.message = message;
        }
    }
}

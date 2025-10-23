package org.moviesystem.back.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.moviesystem.back.dao.CoordinatesDAO;
import org.moviesystem.back.dao.MovieDAO;
import org.moviesystem.back.dao.PersonDAO;
import org.moviesystem.back.model.Coordinates;
import org.moviesystem.back.model.Movie;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы с фильмами.
 * 
 * Этот класс содержит бизнес-логику приложения и координирует работу между
 * различными DAO (Data Access Objects). Он отвечает за:
 * 
 * - Создание фильмов с правильной обработкой связанных объектов
 * - Управление транзакциями
 * - Валидацию бизнес-правил
 * - Отправку событий для уведомления других компонентов
 * 
 * Основные функции:
 * 1. Создание фильмов - проверяет существование связанных объектов (координаты, люди)
 * 2. Управление фильмами - CRUD операции
 * 3. Специальные операции - перераспределение Оскаров, статистика
 * 
 * @author Movie Management System Team
 * @version 1.0
 */

@ApplicationScoped
public class MovieService {
    
    @Inject
    private MovieDAO movieDAO;
    
    @Inject
    private Event<Movie> movieEvent;

    @Inject
    private CoordinatesDAO coordinatesDAO;

    @Inject
    private PersonDAO personDAO;
    
    /**
     * Создает новый фильм с правильной обработкой связанных объектов.
     * 
     * Этот метод выполняет сложную логику создания фильма:
     * 
     * 1. Координаты (Coordinates):
     *    - Если передан ID → ищем существующие координаты
     *    - Если ID нет → проверяем по x,y, если найдены → используем существующие
     *    - Если не найдены → создаем новые
     * 
     * 2. Люди (Director, Screenwriter, Operator):
     *    - Если передан ID → ищем существующего человека
     *    - Если ID нет → создаем нового человека
     * 
     * 3. Сохранение фильма и отправка события
     * 
     * @param movie объект фильма для создания
     * @return созданный фильм
     * @throws Exception если произошла ошибка при создании
     */
    public Movie createMovie(Movie movie) {
        // Обрабатываем координаты: если ID есть - ищем существующие, если нет - проверяем по x,y или создаем новые
        if (movie.getCoordinates() != null) {
            if (movie.getCoordinates().getId() != null) {
                // Ищем существующие координаты по ID
                movie.setCoordinates(coordinatesDAO.findById(movie.getCoordinates().getId()));
            } else {
                // Проверяем, есть ли уже координаты с такими x,y
                Coordinates existingCoordinates = coordinatesDAO.findByXAndY(
                    movie.getCoordinates().getX(), 
                    movie.getCoordinates().getY()
                );
                
                if (existingCoordinates != null) {
                    // Используем существующие координаты
                    movie.setCoordinates(existingCoordinates);
                } else {
                    // Создаем новые координаты
                    movie.setCoordinates(coordinatesDAO.create(movie.getCoordinates()));
                }
            }
        }
        
        // Обрабатываем режиссера: если ID есть - ищем существующего, если нет - создаем нового
        if (movie.getDirector() != null) {
            if (movie.getDirector().getId() != null) {
                // Ищем существующего режиссера
                movie.setDirector(personDAO.findById(movie.getDirector().getId()));
            } else {
                // Создаем нового режиссера
                movie.setDirector(personDAO.create(movie.getDirector()));
            }
        }
        
        // Обрабатываем сценариста: если ID есть - ищем существующего, если нет - создаем нового
        if (movie.getScreenwriter() != null) {
            if (movie.getScreenwriter().getId() != null) {
                // Ищем существующего сценариста
                movie.setScreenwriter(personDAO.findById(movie.getScreenwriter().getId()));
            } else {
                // Создаем нового сценариста
                movie.setScreenwriter(personDAO.create(movie.getScreenwriter()));
            }
        }
        
        // Обрабатываем оператора: если ID есть - ищем существующего, если нет - создаем нового
        if (movie.getOperator() != null) {
            if (movie.getOperator().getId() != null) {
                // Ищем существующего оператора
                movie.setOperator(personDAO.findById(movie.getOperator().getId()));
            } else {
                // Создаем нового оператора
                movie.setOperator(personDAO.create(movie.getOperator()));
            }
        }
        
        Movie createdMovie = movieDAO.create(movie);
        movieEvent.fire(createdMovie);
        return createdMovie;
    }
    
    public Optional<Movie> getMovieById(Long id) {
        return movieDAO.findById(id);
    }
    
    public List<Movie> getAllMovies() {
        return movieDAO.findAll();
    }
    
    public List<Movie> getMovies(int page, int size, String sortField, String sortOrder) {
        return movieDAO.findAll(page, size, sortField, sortOrder);
    }
    
    public List<Movie> getMovies(int page, int size) {
        return movieDAO.findAll(page, size, "id", "asc");
    }
    
    public long getMoviesCount() {
        return movieDAO.countAll();
    }
    
    public List<Movie> searchMoviesByName(String name, String sortField, String sortOrder) {
        return movieDAO.findByNameContaining(name, sortField, sortOrder);
    }
    
    public List<Movie> searchMoviesByName(String name) {
        return movieDAO.findByNameContaining(name, "id", "asc");
    }
    
    public List<Movie> getMoviesByGenre(String genre, String sortField, String sortOrder) {
        return movieDAO.findByGenre(genre, sortField, sortOrder);
    }
    
    public List<Movie> getMoviesByGenre(String genre) {
        return movieDAO.findByGenre(genre, "id", "asc");
    }
    
    public List<Movie> getMoviesByDirector(String directorName, String sortField, String sortOrder) {
        return movieDAO.findByDirector(directorName, sortField, sortOrder);
    }
    
    public List<Movie> getMoviesByDirector(String directorName) {
        return movieDAO.findByDirector(directorName, "id", "asc");
    }
    
    public List<Movie> getMoviesByOscarsRange(int minOscars, int maxOscars, String sortField, String sortOrder) {
        return movieDAO.findByOscarsRange(minOscars, maxOscars, sortField, sortOrder);
    }
    
    public List<Movie> getMoviesByOscarsRange(int minOscars, int maxOscars) {
        return movieDAO.findByOscarsRange(minOscars, maxOscars, "id", "asc");
    }
    
    public Movie updateMovie(Movie movie) {
        Movie updatedMovie = movieDAO.update(movie);
        movieEvent.fire(updatedMovie);
        return updatedMovie;
    }
    
    public void deleteMovie(Long id) {
        // Просто удаляем фильм - НЕ загружаем объект в кэш!
        movieDAO.delete(id);
        movieEvent.fire(new Movie()); // Событие об удалении
    }
    
    public void deleteMoviesByGoldenPalmCount(Long goldenPalmCount) {
        movieDAO.deleteByGoldenPalmCount(goldenPalmCount);
        movieEvent.fire(new Movie()); // Событие об удалении
    }
    
    public Long getSumGoldenPalmCount() {
        return movieDAO.sumGoldenPalmCount();
    }
    
    public List<Movie> getMoviesByGoldenPalmCountGreaterThan(Long goldenPalmCount) {
        return movieDAO.findByGoldenPalmCountGreaterThan(goldenPalmCount);
    }
    
    public List<Movie> getMoviesByGoldenPalmCount(Long goldenPalmCount) {
        return movieDAO.findByGoldenPalmCount(goldenPalmCount);
    }
    
    public List<String> getAllGenres() {
        return movieDAO.getAllGenres();
    }
    
    public void redistributeOscars(String fromGenre, String toGenre) {
        movieDAO.redistributeOscars(fromGenre, toGenre);
        movieEvent.fire(new Movie()); // Событие об изменении
    }
    
    public void addOscarsToLongMovies(Long minLength, int oscarsToAdd) {
        movieDAO.addOscarsToLongMovies(minLength, oscarsToAdd);
        movieEvent.fire(new Movie()); // Событие об изменении
    }
}

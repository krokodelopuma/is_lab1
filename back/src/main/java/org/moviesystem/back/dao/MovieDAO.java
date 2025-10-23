package org.moviesystem.back.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.moviesystem.back.model.Movie;
import org.moviesystem.back.model.MovieGenre;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) для работы с фильмами в базе данных.
 * 
 * Этот класс отвечает за все операции с базой данных, связанные с фильмами:
 * - Создание новых фильмов
 * - Поиск фильмов по различным критериям
 * - Обновление существующих фильмов
 * - Удаление фильмов
 * - Специальные операции (перераспределение Оскаров, подсчет статистики)
 * 
 * Использует JPA (Java Persistence API) для работы с базой данных PostgreSQL.
 * Все операции выполняются в транзакциях для обеспечения целостности данных.
 * 
 * @author Movie Management System Team
 * @version 1.0
 */
@ApplicationScoped
public class MovieDAO {
    
    /**
     * EntityManager - основной интерфейс JPA для работы с базой данных.
     * Автоматически инжектируется контейнером CDI.
     * Использует persistence unit "default" из persistence.xml
     */
    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;
    
    /**
     * Создает новый фильм в базе данных.
     * 
     * @param movie объект фильма для создания
     * @return созданный фильм с присвоенным ID
     * @throws Exception если произошла ошибка при сохранении
     */
    public Movie create(Movie movie) {
        // Начинаем транзакцию для обеспечения целостности данных
        entityManager.getTransaction().begin();
        try {
            // Сохраняем фильм в базе данных
            entityManager.persist(movie);
            // Принудительно записываем изменения в БД
            entityManager.flush();
            // Подтверждаем транзакцию
            entityManager.getTransaction().commit();
            return movie;
        } catch (Exception e) {
            // В случае ошибки откатываем транзакцию
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public Optional<Movie> findById(Long id) {
        Movie movie = entityManager.find(Movie.class, id);
        return Optional.ofNullable(movie);
    }
    
    public List<Movie> findAll() {
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m ORDER BY m.id", Movie.class);
        return query.getResultList();
    }
    
    public List<Movie> findAll(int page, int size, String sortField, String sortOrder) {
        String orderBy = validateSortField(sortField) + " " + (sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m ORDER BY " + orderBy, 
            Movie.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
    
    public List<Movie> findAll(int page, int size) {
        return findAll(page, size, "id", "asc");
    }
    
    private String validateSortField(String sortField) {
        // Разрешенные поля для сортировки
        switch (sortField.toLowerCase()) {
            case "id": return "m.id";
            case "name": return "m.name";
            case "creationdate": return "m.creationDate";
            case "oscarscount": return "m.oscarsCount";
            case "budget": return "m.budget";
            case "totalboxoffice": return "m.totalBoxOffice";
            case "mpaarating": return "m.mpaaRating";
            case "length": return "m.length";
            case "goldenpalmcount": return "m.goldenPalmCount";
            case "genre": return "m.genre";
            default: return "m.id";
        }
    }
    
    public long countAll() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM Movie m", Long.class);
        return query.getSingleResult();
    }
    
    public List<Movie> findByNameContaining(String name, String sortField, String sortOrder) {
        String orderBy = validateSortField(sortField) + " " + (sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.director d " +
            "LEFT JOIN m.screenwriter s " +
            "LEFT JOIN m.operator o " +
            "WHERE LOWER(m.name) LIKE LOWER(:name) " +
            "OR LOWER(m.genre) LIKE LOWER(:name) " +
            "OR LOWER(d.name) LIKE LOWER(:name) " +
            "OR LOWER(s.name) LIKE LOWER(:name) " +
            "OR LOWER(o.name) LIKE LOWER(:name) " +
            "ORDER BY " + orderBy, 
            Movie.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }
    
    public List<Movie> findByNameContaining(String name) {
        return findByNameContaining(name, "id", "asc");
    }
    
    public List<Movie> findByGenre(String genre, String sortField, String sortOrder) {
        try {
            String orderBy = validateSortField(sortField) + " " + (sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC");
            TypedQuery<Movie> query = entityManager.createQuery(
                "SELECT m FROM Movie m WHERE m.genre = :genre ORDER BY " + orderBy, 
                Movie.class);
            query.setParameter("genre", MovieGenre.valueOf(genre));
            return query.getResultList();
        } catch (IllegalArgumentException e) {
            // Если жанр не найден, возвращаем пустой список
            return List.of();
        }
    }
    
    public List<Movie> findByGenre(String genre) {
        return findByGenre(genre, "id", "asc");
    }
    
    public List<Movie> findByDirector(String directorName, String sortField, String sortOrder) {
        String orderBy = validateSortField(sortField) + " " + (sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m JOIN m.director d WHERE LOWER(d.name) LIKE LOWER(:name) ORDER BY " + orderBy, 
            Movie.class);
        query.setParameter("name", "%" + directorName + "%");
        return query.getResultList();
    }
    
    public List<Movie> findByDirector(String directorName) {
        return findByDirector(directorName, "id", "asc");
    }
    
    public List<Movie> findByOscarsRange(int minOscars, int maxOscars, String sortField, String sortOrder) {
        String orderBy = validateSortField(sortField) + " " + (sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC");
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m WHERE m.oscarsCount >= :minOscars AND m.oscarsCount <= :maxOscars ORDER BY " + orderBy, 
            Movie.class);
        query.setParameter("minOscars", minOscars);
        query.setParameter("maxOscars", maxOscars);
        return query.getResultList();
    }
    
    public List<Movie> findByOscarsRange(int minOscars, int maxOscars) {
        return findByOscarsRange(minOscars, maxOscars, "id", "asc");
    }
    
    public List<Movie> findByGoldenPalmCountGreaterThan(Long goldenPalmCount) {
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m WHERE m.goldenPalmCount IS NOT NULL AND m.goldenPalmCount > :goldenPalmCount ORDER BY m.id", 
            Movie.class);
        query.setParameter("goldenPalmCount", goldenPalmCount);
        return query.getResultList();
    }
    
    public List<Movie> findByGoldenPalmCount(Long goldenPalmCount) {
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m WHERE m.goldenPalmCount = :goldenPalmCount ORDER BY m.id", 
            Movie.class);
        query.setParameter("goldenPalmCount", goldenPalmCount);
        return query.getResultList();
    }
    
    public List<Movie> findByLengthGreaterThan(Long length) {
        TypedQuery<Movie> query = entityManager.createQuery(
            "SELECT m FROM Movie m WHERE m.length > :length ORDER BY m.id", 
            Movie.class);
        query.setParameter("length", length);
        return query.getResultList();
    }
    
    public Movie update(Movie movie) {
        entityManager.getTransaction().begin();
        try {
            Movie updatedMovie = entityManager.merge(movie);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return updatedMovie;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public void delete(Long id) {
        entityManager.getTransaction().begin();
        try {
            System.out.println("DEBUG: Удаляем фильм с ID: " + id);
            System.out.println("DEBUG: Используем ТОЛЬКО нативный SQL без find()");
            
            // Используем ТОЛЬКО нативный SQL запрос - НЕ делаем find()!
            int deletedCount = entityManager.createNativeQuery("DELETE FROM movies WHERE id = ?")
                .setParameter(1, id)
                .executeUpdate();
            
            if (deletedCount == 0) {
                throw new IllegalArgumentException("Фильм с ID " + id + " не найден");
            }
            
            entityManager.flush();
            entityManager.getTransaction().commit();
            
            System.out.println("DEBUG: Фильм успешно удален с ID: " + id);
            System.out.println("DEBUG: Связанные объекты сохранены в базе данных");
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            System.out.println("DEBUG: Ошибка при удалении фильма: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public void deleteByGoldenPalmCount(Long goldenPalmCount) {
        entityManager.getTransaction().begin();
        try {
            System.out.println("DEBUG: Удаляем фильмы с " + goldenPalmCount + " Золотыми пальмами");
            System.out.println("DEBUG: Используем нативный SQL для обхода всех проблем с JPA");
            
            // Используем нативный SQL запрос для удаления - это гарантированно работает
            int deletedCount = entityManager.createNativeQuery(
                "DELETE FROM movies WHERE golden_palm_count = ?")
                .setParameter(1, goldenPalmCount)
                .executeUpdate();
            
            entityManager.flush();
            entityManager.getTransaction().commit();
            
            System.out.println("DEBUG: Удалено фильмов с " + goldenPalmCount + " Золотыми пальмами: " + deletedCount);
            System.out.println("DEBUG: Связанные объекты сохранены в базе данных");
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            System.out.println("DEBUG: Ошибка при удалении фильмов: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Long sumGoldenPalmCount() {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT COALESCE(SUM(m.goldenPalmCount), 0) FROM Movie m WHERE m.goldenPalmCount IS NOT NULL AND m.goldenPalmCount > 0", 
            BigDecimal.class);
        BigDecimal result = query.getSingleResult();
        return result != null ? result.longValue() : 0L;
    }
    
    public List<String> getAllGenres() {
        TypedQuery<String> query = entityManager.createQuery(
            "SELECT DISTINCT m.genre FROM Movie m ORDER BY m.genre", 
            String.class);
        return query.getResultList();
    }
    
    /**
     * Перераспределяет Оскары между жанрами фильмов.
     * 
     * Логика работы:
     * 1. Собирает все Оскары с фильмов исходного жанра
     * 2. Обнуляет Оскары у всех фильмов исходного жанра
     * 3. Обнуляет Оскары у всех фильмов целевого жанра
     * 4. Равномерно распределяет все собранные Оскары между фильмами целевого жанра
     * 
     * Пример: если у жанра ADVENTURE есть 12 Оскаров (6+4+2), а у DRAMA 2 фильма,
     * то каждый фильм DRAMA получит по 6 Оскаров (12/2).
     * 
     * @param fromGenre жанр, с которого забираем Оскары (например, "ADVENTURE")
     * @param toGenre жанр, которому отдаем Оскары (например, "DRAMA")
     * @throws IllegalArgumentException если жанры неверные или одинаковые
     * @throws Exception если произошла ошибка при перераспределении
     */
    public void redistributeOscars(String fromGenre, String toGenre) {
        entityManager.getTransaction().begin();
        try {
            // Валидация жанров
            if (fromGenre == null || toGenre == null || fromGenre.trim().isEmpty() || toGenre.trim().isEmpty()) {
                throw new IllegalArgumentException("Жанры не могут быть пустыми");
            }
            
            if (fromGenre.equals(toGenre)) {
                throw new IllegalArgumentException("Исходный и целевой жанры не могут быть одинаковыми");
            }
            
            // Преобразуем строки в enum
            MovieGenre fromGenreEnum;
            MovieGenre toGenreEnum;
            
            try {
                fromGenreEnum = MovieGenre.valueOf(fromGenre);
                toGenreEnum = MovieGenre.valueOf(toGenre);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверный жанр: " + e.getMessage());
            }
            
            // Получаем все фильмы исходного жанра
            TypedQuery<Movie> fromMoviesQuery = entityManager.createQuery(
                "SELECT m FROM Movie m WHERE m.genre = :fromGenre", Movie.class);
            fromMoviesQuery.setParameter("fromGenre", fromGenreEnum);
            List<Movie> fromMovies = fromMoviesQuery.getResultList();
            
            // Получаем все фильмы целевого жанра
            TypedQuery<Movie> toMoviesQuery = entityManager.createQuery(
                "SELECT m FROM Movie m WHERE m.genre = :toGenre", Movie.class);
            toMoviesQuery.setParameter("toGenre", toGenreEnum);
            List<Movie> toMovies = toMoviesQuery.getResultList();
            
            if (fromMovies.isEmpty()) {
                List<String> availableGenres = getAllGenres();
                throw new IllegalArgumentException("Не найдено фильмов жанра: " + fromGenre + 
                    ". Доступные жанры: " + String.join(", ", availableGenres));
            }
            
            if (toMovies.isEmpty()) {
                List<String> availableGenres = getAllGenres();
                throw new IllegalArgumentException("Не найдено фильмов жанра: " + toGenre + 
                    ". Доступные жанры: " + String.join(", ", availableGenres));
            }
            
            // Суммируем все Оскары исходного жанра
            int totalOscars = fromMovies.stream()
                .mapToInt(Movie::getOscarsCount)
                .sum();
            
            System.out.println("DEBUG: Перераспределение Оскаров");
            System.out.println("DEBUG: Исходный жанр: " + fromGenre + ", фильмов: " + fromMovies.size());
            System.out.println("DEBUG: Целевой жанр: " + toGenre + ", фильмов: " + toMovies.size());
            System.out.println("DEBUG: Всего Оскаров для перераспределения: " + totalOscars);
            
            if (totalOscars == 0) {
                System.out.println("DEBUG: Нет Оскаров для перераспределения");
                entityManager.getTransaction().commit();
                return; // Нет Оскаров для перераспределения
            }
            
            // Обнуляем Оскары у исходного жанра
            for (Movie movie : fromMovies) {
                System.out.println("DEBUG: Обнуляем Оскары у фильма: " + movie.getName() + " (было: " + movie.getOscarsCount() + ")");
                movie.setOscarsCount(0);
                entityManager.merge(movie);
            }
            
            // Сначала обнуляем Оскары у целевого жанра
            for (Movie movie : toMovies) {
                System.out.println("DEBUG: Обнуляем Оскары у фильма целевого жанра: " + movie.getName() + " (было: " + movie.getOscarsCount() + ")");
                movie.setOscarsCount(0);
                entityManager.merge(movie);
            }
            
            // Равномерно распределяем все Оскары между фильмами целевого жанра
            int oscarsPerMovie = totalOscars / toMovies.size();
            int remainingOscars = totalOscars % toMovies.size();
            
            System.out.println("DEBUG: Оскаров на фильм: " + oscarsPerMovie + ", остаток: " + remainingOscars);
            
            for (int i = 0; i < toMovies.size(); i++) {
                Movie movie = toMovies.get(i);
                int oscarsToSet = oscarsPerMovie + (i < remainingOscars ? 1 : 0);
                movie.setOscarsCount(oscarsToSet);
                System.out.println("DEBUG: Устанавливаем " + oscarsToSet + " Оскаров фильму: " + movie.getName());
                entityManager.merge(movie);
            }
            
            System.out.println("DEBUG: Сохраняем изменения...");
            entityManager.flush();
            entityManager.getTransaction().commit();
            
            // Принудительно очищаем кеш для обновления данных
            entityManager.clear();
            
            System.out.println("DEBUG: Перераспределение завершено успешно");
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
    
    public void addOscarsToLongMovies(Long minLength, int oscarsToAdd) {
        entityManager.getTransaction().begin();
        try {
            TypedQuery<Movie> query = entityManager.createQuery(
                "SELECT m FROM Movie m WHERE m.length > :minLength", Movie.class);
            query.setParameter("minLength", minLength);
            List<Movie> movies = query.getResultList();
            
            for (Movie movie : movies) {
                movie.setOscarsCount(movie.getOscarsCount() + oscarsToAdd);
                entityManager.merge(movie);
            }
            entityManager.flush();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
}

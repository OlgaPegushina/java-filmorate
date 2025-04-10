package ru.yandex.practicum.filmorate.dal.storage.film;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.LikeUserIdsRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.dal.storage.director.DirectorDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbRepository")
@Primary // Устанавливаем FilmDbRepository как первичный бин
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilmDbRepository extends BaseRepository<Film> implements FilmStorage {
    RowMapper<Film> mapper = new FilmRowMapper();
    MpaDbRepository mpaDbRepository;
    UserStorage userStorage;
    GenreDbRepository genreDbRepository;
    DirectorDbRepository directorDbRepository;

    public FilmDbRepository(JdbcTemplate jdbc, MpaDbRepository mpaDbRepository, UserStorage userStorage,
                            GenreDbRepository genreDbRepository, DirectorDbRepository directorDbRepository) {
        super(jdbc);
        this.mpaDbRepository = mpaDbRepository;
        this.userStorage = userStorage;
        this.genreDbRepository = genreDbRepository;
        this.directorDbRepository = directorDbRepository;
    }

    @Override
    public Film create(Film film) {
        RatingMpa mpa = mpaDbRepository.getMpaById(film.getMpa().getId());

        String query = "INSERT INTO film(name, description, release_date, " +
                "duration_in_minutes, rating_id) VALUES (?, ?, ?, ?, ?)";
        long id = super.create(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenres(film.getId(), film.getGenres());
        }

        film.setMpa(mpa);

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            addDirectorFilms(film.getId(), film.getDirectors());
        }

        return film;
    }

    @Override
    public Film getById(Long id) {
        String query = "SELECT * FROM film WHERE film_id = ?";
        Film film = findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден.", id)));
        Set<Long> likes = getLikeUserIds(id);
        film.setLikes(likes);
        List<Genre> genres = getGenre(id);
        film.setGenres(Objects.requireNonNullElseGet(genres, ArrayList::new));
        film.setMpa(mpaDbRepository.getMpaById(film.getMpa().getId()));
        List<Director> directors = findDirectors(id);
        film.setDirectors(directors);
        return film;
    }

    @Override
    public Map<Long, Film> getAll() {
        return getAllValues().stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
    }

    @Override
    public List<Film> getAllValues() {
        String query = "SELECT * FROM film";
        List<Film> films = findMany(query, mapper);
        return setFilms(films);
    }

    @Override
    public Film update(Film film) {
        RatingMpa mpa = mpaDbRepository.getMpaById(film.getMpa().getId());
        String query = "UPDATE film SET name = ?, description = ?, release_date = ?, duration_in_minutes = ?, " +
                "rating_id = ? WHERE film_id = ?";
        super.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpa.getId(),
                film.getId()
        );

        deleteFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenres(film.getId(), film.getGenres());
        }
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            for (Long userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        deleteDirectorFilms(film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            addDirectorFilms(film.getId(), film.getDirectors());
        }
        return film;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM film WHERE film_id = ?";
        boolean isDeleted = super.delete(query, id);

        if (!isDeleted) {
            throw new InternalServerException(String.format("Не удалось удалить фильм с id: %d", id));
        }

        deleteFilmGenres(id);
        deleteDirectorFilms(id);
    }

    @Override
    public Set<Long> getLikeUserIds(long id) {
        String query = "SELECT user_id FROM film_users WHERE film_id = ?";
        return new HashSet<>(jdbc.query(query, new LikeUserIdsRowMapper(), id));
    }

    @Override
    public List<Genre> getGenre(long id) {
        String query = "SELECT g.genre_id, g.name FROM film_genre fg " +
                "INNER JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return new ArrayList<>(jdbc.query(query, new GenreRowMapper(), id));
    }

    @Override
    public List<Film> getPopularFilms(Long genreId, Integer year, int count) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT f.*, COUNT(fu.user_id) AS like_count ")
                .append("FROM film f ")
                .append("LEFT JOIN film_users fu ON f.film_id = fu.film_id ")
                .append("LEFT JOIN film_genre fg ON f.film_id = fg.film_id ")
                .append("LEFT JOIN genre g ON fg.genre_id = g.genre_id ")
                .append("WHERE 1=1 "); // Условие для использования всегда AND

        if (genreId != null) {
            queryBuilder.append("AND (g.genre_id = ?) ");
        }

        if (year != null) {
            queryBuilder.append("AND (YEAR(f.release_date) = ?) ");
        }

        queryBuilder.append("GROUP BY f.film_id ")
                .append("ORDER BY like_count DESC ")
                .append("LIMIT ?");

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            params.add(genreId);
        }

        if (year != null) {
            params.add(year);
        }

        params.add(count);

        List<Film> popularFilms = findMany(queryBuilder.toString(), mapper, params.toArray());

        return setFilms(popularFilms);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        if (!likeExists(filmId, userId)) {
            String query = "INSERT INTO film_users (film_id, user_id) VALUES (?, ?)";
            super.update(query, filmId, userId);
        }
    }

    public boolean likeExists(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM film_users WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        getById(filmId);
        userStorage.getById(userId);
        String query = "DELETE FROM film_users WHERE film_id = ? and user_id = ?";
        super.update(query, filmId, userId);
    }

    @Override
    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        String query = "SELECT f.*, COUNT(fu.user_id) AS like_count " +
                "FROM film f " +
                "JOIN director_film df ON f.film_id = df.film_id " +
                "LEFT JOIN film_users fu ON f.film_id = fu.film_id " +
                "WHERE df.director_id = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY " +
                (sortBy.equals("likes") ? "like_count DESC" : "f.release_date");

        List<Film> films = findMany(query, mapper, directorId);
        return setFilms(films);
    }

    @Override
    public List<Film> searchFilms(String strQuery, String searchIn) {
        StringBuilder query = new StringBuilder("SELECT f.* FROM film f ");
        query.append("LEFT JOIN director_film df ON f.film_id = df.film_id ");
        query.append("LEFT JOIN director d ON df.director_id = d.director_id ");
        query.append("LEFT JOIN film_users fu ON f.film_id = fu.film_id ");
        query.append("WHERE ");

        List<String> conditions = new ArrayList<>();
        List<String> params = new ArrayList<>();

        if (searchIn.contains("title")) {
            conditions.add("UPPER(f.name) LIKE UPPER(?)");
            params.add("%" + strQuery + "%");
        }
        if (searchIn.contains("director")) {
            conditions.add("UPPER(d.name) LIKE UPPER(?)");
            params.add("%" + strQuery + "%");
        }

        query.append(String.join(" OR ", conditions));

        query.append(" GROUP BY f.film_id ");

        query.append(" ORDER BY COUNT(fu.user_id) DESC");

        List<Film> films = findMany(query.toString(), mapper, params.toArray());
        return setFilms(films);
    }

    @Override
    public Set<Long> getLikedFilmIdsByUser(Long userId) {
        String sql = "SELECT film_id FROM film_users WHERE user_id = ?";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) -> rs.getLong("film_id"), userId));
    }

    @Override
    public Map<Long, Integer> getUsersWithCommonLikes(Long userId, Set<Long> likedFilmIds) {
        if (likedFilmIds.isEmpty()) return Map.of();

        String inSql = likedFilmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
                    SELECT user_id, COUNT(*) AS common
                    FROM film_users
                    WHERE film_id IN (%s) AND user_id != ?
                    GROUP BY user_id
                    ORDER BY common DESC
                """, inSql);

        List<Object> params = new ArrayList<>(likedFilmIds);
        params.add(userId);

        return jdbc.query(sql, rs -> {
            Map<Long, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getLong("user_id"), rs.getInt("common"));
            }
            return result;
        }, params.toArray());
    }

    @Override
    public List<Film> findRecommendedFilmsForUser(Set<Long> similarUserIds, Set<Long> excludedFilmIds) {
        if (similarUserIds.isEmpty()) return List.of();

        String usersSql = similarUserIds.stream().map(u -> "?").collect(Collectors.joining(","));
        String excludedSql = excludedFilmIds.stream().map(f -> "?").collect(Collectors.joining(","));

        String sql = String.format("""
                    SELECT DISTINCT f.*
                    FROM film f
                    JOIN film_users fu ON f.film_id = fu.film_id
                    WHERE fu.user_id IN (%s)
                    AND fu.film_id NOT IN (%s)
                """, usersSql, excludedSql);

        List<Object> params = new ArrayList<>(similarUserIds);
        params.addAll(excludedFilmIds);

        return jdbc.query(sql, mapper, params.toArray());
    }

    private List<Film> setFilms(List<Film> films) {
        for (Film film : films) {
            film.setLikes(getLikeUserIds(film.getId()));
            film.setGenres(getGenre(film.getId()));
            film.setMpa(mpaDbRepository.getMpaById(film.getMpa().getId()));
            film.setDirectors(findDirectors(film.getId()));
        }
        return films;
    }

    private boolean deleteFilmGenres(Long filmId) {
        String query = "DELETE FROM film_genre WHERE film_id = ?";
        int rowsDeleted = jdbc.update(query, filmId);
        return rowsDeleted > 0;
    }

    private boolean addFilmGenres(Long filmId, List<Genre> genres) {
        for (Genre genre : genres) {
            genreDbRepository.getGenreById(genre.getId());
            String query = "MERGE INTO film_genre AS target " +
                    "KEY (film_id, genre_id) " +
                    "VALUES (?, ?)";
            jdbc.update(query, filmId, genre.getId());
        }
        return true;
    }

    private boolean addDirectorFilms(Long filmId, List<Director> directors) {
        for (Director director : directors) {
            directorDbRepository.getById(director.getId());
            String query = "MERGE INTO director_film AS target " +
                    "KEY (director_id, film_id) " +
                    "VALUES (?, ?)";
            jdbc.update(query, director.getId(), filmId);
        }
        return true;
    }

    private boolean deleteDirectorFilms(Long filmId) {
        String query = "DELETE FROM director_film WHERE film_id = ?";
        int rowsDeleted = jdbc.update(query, filmId);
        return rowsDeleted > 0;
    }

    private List<Director> findDirectors(long film_id) {
        String query = "SELECT d.director_id, d.name FROM director_film df " +
                "INNER JOIN director d ON df.director_id = d.director_id " +
                "WHERE df.film_id = ?";
        return new ArrayList<>(jdbc.query(query, new DirectorRowMapper(), film_id));
    }

    public Map<Integer, List<Genre>> getAllFilmGenres(Collection<Film> films) {
        Map<Integer, List<Genre>> filmGenreMap = new HashMap<>();
        Collection<String> ids = films.stream()
                .map(film -> String.valueOf(film.getId()))
                .toList();

        String query = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (%s)";

        jdbc.query(String.format(query, String.join(",", ids)), rs -> {
            Genre genre = Genre.builder()
                    .id(rs.getInt("genre_id"))
                    .name(rs.getString("name"))
                    .build();

            Integer filmId = rs.getInt("film_id");

            filmGenreMap.putIfAbsent(filmId, new ArrayList<>());
            filmGenreMap.get(filmId).add(genre);
        });
        return filmGenreMap;
    }


    public Collection<Film> getCommonFilms(Integer userId, Integer friendId) {
        String query = """
                SELECT f.*
                FROM film_users fu1
                JOIN film_users fu2 ON fu1.film_id = fu2.film_id
                JOIN film f ON fu1.film_id = f.film_id
                WHERE fu1.user_id = ?
                  AND fu2.user_id = ?
                GROUP BY f.film_id
                ORDER BY (SELECT COUNT(*) FROM film_users WHERE film_users.film_id = f.film_id) DESC;""";
        return findMany(query, mapper, userId, friendId);
    }
}


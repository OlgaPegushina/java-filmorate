package ru.yandex.practicum.filmorate.dal.storage.film;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.LikeUserIdsRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.dal.storage.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbRepository")
@Primary // Устанавливаем FilmDbRepository как первичный бин
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilmDbRepository extends BaseRepository<Film> implements FilmRepository {
    RowMapper<Film> mapper = new FilmRowMapper();
    MpaDbRepository mpaDbRepository;
    UserStorage userStorage;
    GenreDbRepository genreDbRepository;

    public FilmDbRepository(JdbcTemplate jdbc, MpaDbRepository mpaDbRepository, UserStorage userStorage,
                            GenreDbRepository genreDbRepository) {
        super(jdbc);
        this.mpaDbRepository = mpaDbRepository;
        this.userStorage = userStorage;
        this.genreDbRepository = genreDbRepository;
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
        film.setLikes(new HashSet<>());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenres(film.getId(), film.getGenres());
        }

        film.setMpa(mpa);

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
        film.setGenres(genres);
        film.setMpa(mpaDbRepository.getMpaById(film.getMpa().getId()));
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
        for (Film film : films) {
            film.setLikes(getLikeUserIds(film.getId()));
            film.setGenres(getGenre(film.getId()));
            film.setMpa(mpaDbRepository.getMpaById(film.getMpa().getId()));
        }
        return films;
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

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String query = "SELECT f.* FROM film f " +
                "JOIN ( " +
                "    SELECT film_id " +
                "    FROM film_users " +
                "    GROUP BY film_id " +
                "    ORDER BY COUNT(user_id) DESC " +
                "    LIMIT ? " +
                ") AS popular ON f.film_id = popular.film_id " +
                "ORDER BY (SELECT COUNT(*) FROM film_users WHERE film_id = f.film_id) DESC;";

        List<Film> popularFilms = findMany(query, mapper, count);
        for (Film film : popularFilms) {
            film.setLikes(getLikeUserIds(film.getId()));
            film.setGenres(getGenre(film.getId()));
            film.setMpa(mpaDbRepository.getMpaById(film.getMpa().getId()));
        }
        return popularFilms;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        userStorage.getById(userId);
        getById(filmId);
        String query = "INSERT INTO film_users (film_id, user_id) VALUES (?, ?)";
        super.update(query, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        getById(filmId);
        userStorage.getById(userId);
        String query = "DELETE FROM film_users WHERE film_id = ? and user_id = ?";
        super.update(query, filmId, userId);
    }
}


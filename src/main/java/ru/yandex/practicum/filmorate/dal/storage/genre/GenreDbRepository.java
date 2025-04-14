package ru.yandex.practicum.filmorate.dal.storage.genre;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GenreDbRepository implements GenreRepository {
    RowMapper<Genre> mapper = new GenreRowMapper();
    JdbcTemplate jdbc;

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        return Optional.ofNullable(jdbc.queryForObject("SELECT * FROM genre WHERE genre_id = ?", mapper, genreId));
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return jdbc.query("SELECT * FROM genre ORDER BY genre_id", mapper);
    }
}

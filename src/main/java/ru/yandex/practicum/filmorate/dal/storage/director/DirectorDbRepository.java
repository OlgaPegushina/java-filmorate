package ru.yandex.practicum.filmorate.dal.storage.director;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("directorDbRepository")
@Primary // Устанавливаем DirectorDbRepository как первичный бин
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorDbRepository extends BaseRepository<Director> implements DirectorStorage {
    RowMapper<Director> mapper = new DirectorRowMapper();

    public DirectorDbRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Director create(Director director) {
        String query = "INSERT INTO director(name) VALUES (?)";
        long id = super.create(query,
                director.getName()
        );
        director.setId(id);

        return director;
    }

    @Override
    public Director getById(Long id) {
        String query = "SELECT * FROM director WHERE director_id = ?";
        return findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id %d не найден.", id)));
    }

    @Override
    public Map<Long, Director> getAll() {
        return getAllValues().stream()
                .collect(Collectors.toMap(Director::getId, director -> director));
    }

    @Override
    public Collection<Director> getAllValues() {
        String query = "SELECT * FROM director";
        return findMany(query, mapper);
    }

    @Override
    public Director update(Director director) {
        String query = "UPDATE director SET name = ? WHERE director_id = ?";
        super.update(query,
                director.getName(),
                director.getId()
        );
        return director;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM director WHERE director_id = ?";
        boolean isDeleted = super.delete(query, id);

        if (!isDeleted) {
            throw new InternalServerException(String.format("Не удалось удалить фильм с id: %d", id));
        }
    }

    @Override
    public List<Director> getByFilmId(Long filmId) {
        String sql = """
                    SELECT d.director_id, d.name
                    FROM director d
                    JOIN film_director fd ON d.director_id = fd.director_id
                    WHERE fd.film_id = ?
                """;
        return jdbc.query(sql, (rs, rowNum) -> Director.builder()
                .id(rs.getLong("director_id"))
                .name(rs.getString("name"))
                .build(), filmId);
    }
}

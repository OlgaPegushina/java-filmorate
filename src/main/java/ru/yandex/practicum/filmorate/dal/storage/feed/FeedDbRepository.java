package ru.yandex.practicum.filmorate.dal.storage.feed;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FeedRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.time.Instant;
import java.util.Collection;

@Repository("feedDbSRepository")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedDbRepository extends BaseRepository<Feed> implements FeedStorage {
    RowMapper<Feed> mapper = new FeedRowMapper();

    public FeedDbRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public void addEvent(Long userId, Long entityId, EventOperation eventOperation, EventType eventType) {
        String query = """
                INSERT INTO feeds (user_id, entity_id, timestamp, event_type, event_operation)
                VALUES (?, ?, ?, ?, ?)""";
        update(query, userId, entityId, Instant.now().toEpochMilli(), eventType.name(), eventOperation.name());
    }

    @Override
    public Collection<Feed> getFeed(Long userId) {
        String query = "SELECT * FROM feeds WHERE user_id = ? ORDER BY timestamp";
        return findMany(query, mapper, userId);
    }
}
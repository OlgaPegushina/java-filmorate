package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(resultSet.getLong("event_id"))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .eventOperation(EventOperation.valueOf(resultSet.getString("event_operation")))
                .timestamp(resultSet.getLong("timestamp"))
                .build();
    }
}
package ru.yandex.practicum.filmorate.dal.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.Collection;

public interface FeedStorage {

    void addEvent(Long userId, Long entityId, EventOperation eventOperation, EventType eventType);

    Collection<Feed> getFeed(Long userId);
}
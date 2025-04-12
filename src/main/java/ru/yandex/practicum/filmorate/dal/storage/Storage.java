package ru.yandex.practicum.filmorate.dal.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Storage<T> {
    T create(T e);

    Optional<T> getById(Long id);

    Map<Long, T> getAll();

    Collection<T> getAllValues();

    T update(T e);

    void deleteById(Long id);
}

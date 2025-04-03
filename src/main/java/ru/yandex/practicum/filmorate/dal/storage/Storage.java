package ru.yandex.practicum.filmorate.dal.storage;

import java.util.Collection;
import java.util.Map;

public interface Storage<T> {
    T create(T e);

    T getById(Long id);

    Map<Long, T> getAll();

    Collection<T> getAllValues();

    T update(T e);

    void deleteById(Long id);
}

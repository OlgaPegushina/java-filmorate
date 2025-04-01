package ru.yandex.practicum.filmorate.dal.storage.film;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Set;

public interface FilmRepository extends FilmStorage {
    Set<Long> getLikeUserIds(long id);

    List<Genre> getGenre(long id);
}

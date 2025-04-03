package ru.yandex.practicum.filmorate.dal.storage.film;

import ru.yandex.practicum.filmorate.dal.storage.Storage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FilmStorage extends Storage<Film> {
    Set<Long> getLikeUserIds(long id);

    List<Genre> getGenre(long id);

    Collection<Film> getPopularFilms(int count);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);
}

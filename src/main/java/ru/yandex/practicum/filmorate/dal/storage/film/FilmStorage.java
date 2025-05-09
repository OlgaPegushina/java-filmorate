package ru.yandex.practicum.filmorate.dal.storage.film;

import ru.yandex.practicum.filmorate.dal.storage.Storage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.enums.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.enums.FilmSortBy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage extends Storage<Film> {
    Set<Long> getLikeUserIds(long id);

    List<Genre> getGenre(long id);

    Collection<Film> getPopularFilms(Long genreId, Integer year, int count);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> findFilmsByDirectorSorted(Long directorId, FilmSortBy sortBy);

    Collection<Film> searchFilms(String strQuery, Set<FilmSearchBy> searchBySet);

    Set<Long> getLikedFilmIdsByUser(Long userId);

    Map<Long, Integer> getUsersWithCommonLikes(Long userId, Set<Long> likedFilmIds);

    List<Film> findRecommendedFilmsForUser(Set<Long> similarUserIds, Set<Long> excludedFilmIds);

    Map<Long, List<Genre>> getAllFilmGenres(Collection<Film> films);

    RatingMpa getRatingMpa(long filmId);

    Collection<Film> getCommonFilms(Long userId, Long friendId);

    void validateFilm(Long filmId);
}

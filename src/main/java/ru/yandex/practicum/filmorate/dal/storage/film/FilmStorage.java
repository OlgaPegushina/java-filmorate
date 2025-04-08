package ru.yandex.practicum.filmorate.dal.storage.film;

import ru.yandex.practicum.filmorate.dal.storage.Storage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage extends Storage<Film> {
    Set<Long> getLikeUserIds(long id);

    List<Genre> getGenre(long id);

    Collection<Film> getPopularFilms(int count);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> findFilmsByDirectorSorted(Long directorId, String sortBy);

    Collection<Film> searchFilms(String strQuery, String searchIn);

    Set<Long> getLikedFilmIdsByUser(Long userId);

    Map<Long, Integer> getUsersWithCommonLikes(Long userId, Set<Long> likedFilmIds);

    List<Film> findRecommendedFilmsForUser(Set<Long> similarUserIds, Set<Long> excludedFilmIds);

    Map<Integer, List<Genre>> getAllFilmGenres(Collection<Film> films);

    Collection<Film> getCommonFilms(Integer userId, Integer friendId);
}

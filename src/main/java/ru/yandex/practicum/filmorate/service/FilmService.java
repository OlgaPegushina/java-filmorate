package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dal.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.enums.FilmSortBy;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilmService {
    FilmStorage filmStorage;
    FeedStorage feedStorage;
    UserStorage userStorage;
    DirectorStorage directorStorage;

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == 0) {
            throw new ValidationException("ID не может быть равен 0");
        }
        return filmStorage.update(film);
    }

    public Film getById(Long filmId) {
        return filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден.", filmId)));
    }

    public Collection<Film> getAllValues() {
        return filmStorage.getAllValues();
    }

    public void delete(Long id) {
        filmStorage.deleteById(id);
    }

    public Collection<Film> getPopularFilms(Long genreId, Integer year, int count) {
        return filmStorage.getPopularFilms(genreId, year, count);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
        feedStorage.addEvent(userId, filmId, EventOperation.ADD, EventType.LIKE);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
        feedStorage.addEvent(userId, filmId, EventOperation.REMOVE, EventType.LIKE);
    }

    public Collection<Film> findFilmsByDirectorSorted(Long directorId, FilmSortBy sortBy) {
        if (sortBy != null) {
            Director director = directorStorage.getById(directorId)
                    .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id %d не найден.", directorId)));
            return filmStorage.findFilmsByDirectorSorted(director.getId(), sortBy);
            }
            return new ArrayList<>();
        }

    public Collection<Film> searchFilms(String strQuery, String searchIn) {
        if (strQuery != null && !strQuery.trim().isEmpty() && searchIn != null && !searchIn.trim().isEmpty()) {
            Set<FilmSearchBy> searchBySet = Arrays.stream(searchIn.split(","))
                    .map(String::toUpperCase)
                    .map(FilmSearchBy::valueOf)
                    .collect(Collectors.toSet());
            return filmStorage.searchFilms(strQuery, searchBySet);
        }
        return new ArrayList<>();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не должна быть ранее 28.12.1895");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);

        Map<Long, List<Genre>> filmGenresMap = filmStorage.getAllFilmGenres(films);
        films.forEach(film -> {
            long filmId = film.getId();
            film.setGenres(filmGenresMap.getOrDefault(filmId, new ArrayList<>()));
            film.setMpa(filmStorage.getRatingMpa(filmId));
        });
        return films;
    }

    private void validateUserExists(Long userId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден: ", userId)));
    }
}

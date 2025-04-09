package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilmService {
    FilmStorage filmStorage;

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
        return filmStorage.getById(filmId);
    }

    public Collection<Film> getAllValues() {
        return filmStorage.getAllValues();
    }

    public Map<Long, Film> getAll() {
        return filmStorage.getAll();
    }

    public void delete(Long id) {
        filmStorage.deleteById(id);
    }

    public Collection<Film> getPopularFilms(Long genreId, Integer year, int count) {
        return filmStorage.getPopularFilms(genreId, year, count);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        if (sortBy != null) {
            return filmStorage.findFilmsByDirectorSorted(directorId, sortBy);
        }
        return new ArrayList<>();
    }

    public Collection<Film> searchFilms(String strQuery, String searchIn) {
        if (strQuery != null && !strQuery.trim().isEmpty() && searchIn != null && !searchIn.trim().isEmpty()) {
            return filmStorage.searchFilms(strQuery, searchIn);
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

    public Collection<Film> getCommonFilms(Integer userId, Integer friendId) {
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
        if (films.isEmpty()) {
            return films;
        }
        Map<Integer, List<Genre>> filmGenresMap = filmStorage.getAllFilmGenres(films);
        films.forEach(film -> {
            long filmId = film.getId();
            film.setGenres(filmGenresMap.getOrDefault((int) filmId, new ArrayList<>()));
        });
        return films;
    }
}

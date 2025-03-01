package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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

    public Collection<Film> getPopularFilms(int count) {
        List<Film> popularFilms =
                getAllValues()
                        .stream()
                        .sorted(Comparator.comparingInt(this::getLikeCount).reversed())
                        .limit(count)
                        .collect(Collectors.toList());
        return popularFilms;
    }

    private int getLikeCount(Film film) {
        return film.getLikes().size();
    }

    public void addLike(Long filmId, Long userId) {
        User user = userStorage.getById(userId);
        Film film = getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " уже добавлял like");
        }
        film.getLikes().add(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        User user = userStorage.getById(userId);
        Film film = getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        film.getLikes().remove(userId);
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
}

package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryFilmStorage")
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private int idFilm = 0;
    private final UserStorage userStorage;

    @Override
    public Film create(Film film) {
        idFilm++;
        film.setId(idFilm);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан: {}", film);
        return film;
    }

    @Override
    public Film getById(Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException("Фильм с ID " + id + " не найден.");
        }

    }

    @Override
    public Map<Long, Film> getAll() {
        return films;
    }

    @Override
    public Collection<Film> getAllValues() {
        return films.values();
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден.");
        }
        return film;
    }

    @Override
    public void deleteById(Long id) {
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            throw new NotFoundException("Фильм с ID " + id + " не найден.");
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<Film> popularFilms =
                getAllValues()
                        .stream()
                        .sorted(Comparator.comparingInt(this::getLikeCount).reversed())
                        .limit(count)
                        .collect(Collectors.toList());
        return popularFilms;
    }

    @Override
    public int getLikeCount(Film film) {
        return film.getLikes().size();
    }

    @Override
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

    @Override
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
}

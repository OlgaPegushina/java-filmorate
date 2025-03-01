package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        log.debug("FilmService({}, {})",
                filmStorage.getClass().getSimpleName(),
                userStorage.getClass().getSimpleName());
        this.filmStorage = filmStorage;
        log.info("Подключена зависимость: {}.", filmStorage.getClass().getName());
        this.userStorage = userStorage;
        log.info("Подключена зависимость: {}.", userStorage.getClass().getName());
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
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
        log.debug("getPopularFilms({}).", count);
         List<Film> popularFilms =
                filmStorage.getAllValues()
                        .stream()
                        .sorted(Comparator.comparingInt(this::getLikeCount).reversed())
                        .limit(count)
                        .collect(Collectors.toList());
        log.trace("Возвращены популярные фильмы: {}.", popularFilms);
        return popularFilms;
    }

    private int getLikeCount(Film film) {
        log.trace("getLikeCount({}).", film);
        log.trace("Возвращено кол-во лайков: {}.", film.getLikes().size());
        return film.getLikes().size();
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("addLike({}, {}).", filmId, userId);
        if (!filmStorage.getAll().containsKey(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (!userStorage.getAll().containsKey(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (filmStorage.getAll().get(filmId).getLikes().contains(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " уже добавлял like");
        }
        filmStorage.getAll().get(filmId).getLikes().add(userId);
        log.debug("Добавлен лайк: {}.", filmStorage.getAll().get(filmId));
    }

    public void deleteLike(Long filmId, Long userId) {
        log.debug("deleteLike({}, {}).", filmId, userId);
        if (!filmStorage.getAll().containsKey(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (!userStorage.getAll().containsKey(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!filmStorage.getAll().get(filmId).getLikes().contains(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " не добавлял like");
        }
        filmStorage.getAll().get(filmId).getLikes().remove(userId);
        log.debug("Лайк удалён: {}.", filmStorage.getAll().get(filmId));
    }


}

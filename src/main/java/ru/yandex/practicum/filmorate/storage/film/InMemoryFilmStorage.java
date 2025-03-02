package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private int idFilm = 0;

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
}

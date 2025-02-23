package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private int idFilm = 0;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        try {
            validateFilm(film);
            idFilm++;
            film.setId(idFilm);
            films.put(film.getId(), film);
            log.info("Фильм успешно создан: {}", film);
            return film;
        } catch (ValidationException e) {
            log.error("Ошибка при создании фильма: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза не должна быть ранее 28.12.1895");
        }
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        try {
            if (film.getId() == 0) {
                throw new ValidationException("ID не может быть равен 0");
            }
            long id = film.getId();

            if (films.containsKey(id)) {
                validateFilm(film);
                film.setId(id);
                films.put(id, film);
                log.info("Фильм обновлен: {}", film);
            } else {
                throw new NotFoundException("Фильм с ID " + film.getId() + " не найден.");
            }
        } catch (ValidationException e) {
            log.error("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        } catch (NotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        }
        return film;
    }
}

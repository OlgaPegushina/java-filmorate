package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAllValues();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        return filmService.getById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(genreId, year, count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(required = false) String sortBy) {
        return filmService.findFilmsByDirectorSorted(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam(name = "query", required = false) String strQuery,
                                        @RequestParam(name = "by", required = false) String searchIn) {
        return filmService.searchFilms(strQuery, searchIn);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Integer userId, @RequestParam Integer friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long filmId) {
        filmService.delete(filmId);
    }
}

package ru.yandex.practicum.filmorate.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestFilmController {

    private FilmController controller;

    @BeforeEach
    public void beforeEach() {
        controller = new FilmController();
    }

    @Test
    public void shouldPassValidation() {
        controller.createFilm(Film.builder()
                .name("name1")
                .description("description1")
                .duration(192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build());

        assertEquals(1, controller.getAllFilms().size());
    }

    @Test
    public void shouldNotPassNameValidation() {
        Film film = Film.builder()
                .name("")
                .description("description1")
                .duration(192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build();

        assertThrows(ValidationException.class, () -> controller.createFilm(film));
    }

    @Test
    public void shouldNotPassDescriptionValidation() {
        Film film = Film.builder()
                .name("name1")
                .description("Фильмов много — и с каждым годом становится всё больше. Чем их больше, тем больше " +
                        "разных оценок. Чем больше оценок, тем сложнее сделать выбор. Однако не время сдаваться! " +
                        "Вы напишете бэкенд для сервиса, который будет работать с фильмами и оценками пользователей, " +
                        "а также возвращать топ-5 фильмов, рекомендованных к просмотру.")
                .duration(192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build();

        assertThrows(ValidationException.class, () -> controller.createFilm(film));
    }

    @Test
    public void shouldNotPassReleaseDateValidation() {
        Film film1 = Film.builder()
                .name("name1")
                .description("description")
                .duration(192)
                .releaseDate(LocalDate.of(1600, 1, 1))
                .build();
        Film film2 = Film.builder()
                .name("name2")
                .description("description2")
                .duration(192)
                .releaseDate(LocalDate.of(2500, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> controller.createFilm(film1));
        assertThrows(ValidationException.class, () -> controller.createFilm(film2));
    }

    @Test
    public void shouldNotPassDurationValidation() {
        Film film = Film.builder()
                .name("name1")
                .description("description1")
                .duration(-192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build();

        assertThrows(ValidationException.class, () -> controller.createFilm(film));
    }

    @Test
    public void emptyFilmShouldNotPassValidation() {
        Film film = Film.builder().build();
        assertThrows(ValidationException.class, () -> controller.createFilm(film));
    }

    @Test
    public void shouldUpdateFilm() {
        controller.createFilm(Film.builder()
                .name("name1")
                .description("description1")
                .duration(192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build());

        controller.updateFilm(Film.builder()
                .id(1)
                .name("name2")
                .description("description2")
                .duration(50)
                .releaseDate(LocalDate.of(2024, 3, 3))
                .build());

        assertEquals(1, controller.getAllFilms().size());
    }

    @Test
    public void shouldPassDescriptionValidationWith200Symbols() {
        controller.createFilm(Film.builder()
                .name("name1")
                .description("description1")
                .duration(192)
                .releaseDate(LocalDate.of(2021, 4, 5))
                .build());

        assertEquals(1, controller.getAllFilms().size());
    }

    @Test
    public void shouldPassReleaseDateValidation() {
        controller.createFilm(Film.builder()
                .name("name1")
                .description("description")
                .duration(192)
                .releaseDate(LocalDate.of(1895, 12, 28))
                .build());

        assertEquals(1, controller.getAllFilms().size());
    }
}
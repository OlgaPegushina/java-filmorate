package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.genre.GenreDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.mpa.MpaDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.user.UserDbRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbRepository.class, UserDbRepository.class, MpaDbRepository.class, GenreDbRepository.class})
@ContextConfiguration(classes = {FilmorateApplication.class})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmDbRepositoryTest {
    FilmDbRepository filmDbRepository;
    UserDbRepository userDbRepository;
    MpaDbRepository mpaDbRepository;

    @Test
    void createTest() {
        Film film = createFilm();
        Film newFilm = filmDbRepository.create(film);

        assertThat(newFilm).hasFieldOrPropertyWithValue("id", newFilm.getId());
        assertThat(newFilm).hasFieldOrPropertyWithValue("name", film.getName());
        assertThat(newFilm).hasFieldOrPropertyWithValue("description", film.getDescription());
        assertThat(newFilm).hasFieldOrPropertyWithValue("releaseDate", film.getReleaseDate());
        assertThat(newFilm).hasFieldOrPropertyWithValue("duration", film.getDuration());
        assertThat(newFilm).hasFieldOrPropertyWithValue("mpa", film.getMpa());

        assertThat(newFilm).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(newFilm).hasFieldOrPropertyWithValue("name", "name");
        assertThat(newFilm).hasFieldOrPropertyWithValue("description", "description");
        assertThat(newFilm).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1999, 9, 9));
        assertThat(newFilm).hasFieldOrPropertyWithValue("duration", 120L);
        assertThat(newFilm).hasFieldOrPropertyWithValue("mpa", mpaDbRepository.getMpaById(3));
    }

    private Film createFilm() {
        return Film
                .builder()
                .name("name")
                .description("description")
                .releaseDate(LocalDate.of(1999, 9, 9))
                .duration(120)
                .mpa(mpaDbRepository.getMpaById(3))
                .build();
    }

    @Test
    void getByIdTest() {
        Film film = createFilm();
        Film newFilm = filmDbRepository.create(film);
        Film filmById = filmDbRepository.getById(newFilm.getId());

        assertThat(filmById).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(filmById).hasFieldOrPropertyWithValue("name", "name");
        assertThat(filmById).hasFieldOrPropertyWithValue("description", "description");
        assertThat(filmById).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1999, 9, 9));
        assertThat(filmById).hasFieldOrPropertyWithValue("duration", 120L);
        assertThat(filmById).hasFieldOrPropertyWithValue("mpa", mpaDbRepository.getMpaById(3));
    }

    @Test
    void getAllTest() {
        Film film1 = filmDbRepository.create(createFilm());
        Film film2 = filmDbRepository.create(createFilm());
        Map<Long, Film> collection = filmDbRepository.getAll();

        assertEquals(collection.size(), 2, "Количество возвращено неверно");
        assertEquals(collection.get(1L), film1, "film1 возвращается неверно");
        assertEquals(collection.get(2L), film2, "film2 возвращается неверно");
    }

    @Test
    void getAllValuesTest() {
        Film film1 = filmDbRepository.create(createFilm());
        Film film2 = filmDbRepository.create(createFilm());
        List<Film> collection = filmDbRepository.getAllValues();

        assertEquals(collection.size(), 2, "Количество возвращено неверно");
        assertEquals(collection.get(0), film1, "film1 возвращается неверно");
        assertEquals(collection.get(1), film2, "film2 возвращается неверно");
    }

    @Test
    void updateTest() {
        Film film = filmDbRepository.create(createFilm());
        Film filmUpdate = Film.builder()
                .id(film.getId())
                .name("name2")
                .description("description2")
                .releaseDate(LocalDate.of(1999, 4, 9))
                .duration(100)
                .mpa(mpaDbRepository.getMpaById(3))
                .build();
        filmDbRepository.update(filmUpdate);
        film = filmDbRepository.getById(1L);

        assertThat(film).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(film).hasFieldOrPropertyWithValue("name", "name2");
        assertThat(film).hasFieldOrPropertyWithValue("description", "description2");
        assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1999, 4, 9));
        assertThat(film).hasFieldOrPropertyWithValue("duration", 100L);
        assertThat(film).hasFieldOrPropertyWithValue("mpa", mpaDbRepository.getMpaById(3));
    }

    @Test
    void deleteByIdTest() {
        Film film1 = filmDbRepository.create(createFilm());
        Film film2 = filmDbRepository.create(createFilm());
        filmDbRepository.deleteById(1L);
        Map<Long, Film> collection = filmDbRepository.getAll();

        assertEquals(collection.size(), 1, "Количество возвращено неверно");
        assertEquals(collection.get(2L), film2, "film2 возвращается неверно");
    }

    @Test
    void addLikeTest() {
        User user1 = User.builder()
                .login("логин")
                .name("Имя")
                .email("email@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user1 = userDbRepository.create(user1);
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user2 = userDbRepository.create(user2);

        Film film = filmDbRepository.create(createFilm());
        filmDbRepository.addLike(film.getId(), user1.getId());
        filmDbRepository.addLike(film.getId(), user2.getId());

        Set<Long> likes = filmDbRepository.getLikeUserIdsFromDB(film.getId());

        assertEquals(likes.size(), 2, "Количество пользователей возвращается неверно");
        assertTrue(likes.contains(user1.getId()));
        assertTrue(likes.contains(user2.getId()));
    }

    @Test
    void deleteLikeTest() {
        User user1 = User.builder()
                .login("логин")
                .name("Имя")
                .email("email@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user1 = userDbRepository.create(user1);
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user2 = userDbRepository.create(user2);

        Film film = filmDbRepository.create(createFilm());
        filmDbRepository.addLike(film.getId(), user1.getId());
        filmDbRepository.addLike(film.getId(), user2.getId());

        filmDbRepository.deleteLike(film.getId(), user1.getId());

        Set<Long> likes = filmDbRepository.getLikeUserIdsFromDB(film.getId());

        assertEquals(likes.size(), 1, "Количество пользователей возвращается неверно");
        assertTrue(likes.contains(user2.getId()));
    }

    @Test
    void getPopularFilmsTest() {
        Film film = filmDbRepository.create(createFilm());
        Film film2 = filmDbRepository.create(createFilm());

        User user1 = User.builder()
                .login("логин")
                .name("Имя")
                .email("email@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user1 = userDbRepository.create(user1);
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        user2 = userDbRepository.create(user2);

        filmDbRepository.addLike(film.getId(), user1.getId());
        filmDbRepository.addLike(film2.getId(), user2.getId());
        filmDbRepository.addLike(film2.getId(), user1.getId());

        Collection<Film> popularFilms = filmDbRepository.getPopularFilms(1);

        assertEquals(popularFilms.size(), 1, "Количество возвращено неверно");
        assertTrue(popularFilms.contains(film2), "film2 возвращается неверно");
    }
}

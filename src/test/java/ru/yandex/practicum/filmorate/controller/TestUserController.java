package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestUserController {
    private UserController controller;

    @BeforeEach
    public void beforeEach() {
        controller = new UserController();
    }

    @Test
    public void shouldPassValidation() {
        controller.createUser(User.builder()
                .login("login1")
                .name("name1")
                .email("mail1@mail.ru")
                .birthday(LocalDate.of(1979, 3, 25))
                .build());

        assertEquals(1, controller.getAllUsers().size());
    }

    @Test
    public void shouldNotPassEmailValidation() {
        User user1 = User.builder()
                .login("login1")
                .name("name1")
                .email("mail1.ru")
                .birthday(LocalDate.of(1979, 3, 25))
                .build();
        User user2 = User.builder()
                .login("login2")
                .name("name2")
                .email("")
                .birthday(LocalDate.of(2003, 6, 8))
                .build();

        assertThrows(ValidationException.class, () -> controller.createUser(user1));
        assertThrows(ValidationException.class, () -> controller.createUser(user2));
    }

    @Test
    public void shouldNotPassLoginValidation() {
        User user1 = User.builder()
                .login("")
                .name("name1")
                .email("mail1@mail.ru")
                .birthday(LocalDate.of(2003, 6, 15))
                .build();
        User user2 = User.builder()
                .login(" login2")
                .name("name2")
                .email("mail2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 15))
                .build();

        assertThrows(ValidationException.class, () -> controller.createUser(user1));
        assertThrows(ValidationException.class, () -> controller.createUser(user2));
    }

    @Test
    public void shouldNotPassBirthdayValidation() {
        User user = User.builder()
                .login("login1")
                .name("name1")
                .email("mail1@mail.ru")
                .birthday(LocalDate.of(2056, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    public void emptyUserShouldNotPassValidation() {
        User user = User.builder().build();

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    public void shouldUpdateUser() {
        controller.createUser(User.builder()
                .login("login1")
                .name("name1")
                .email("mail1@mail.ru")
                .birthday(LocalDate.of(2003, 6, 8))
                .build());

        controller.updateUser(User.builder()
                .id(1)
                .login("login2")
                .name("name2")
                .email("mail2@mail.ru")
                .birthday(LocalDate.of(2003, 6, 8))
                .build());

        assertEquals(1, controller.getAllUsers().size());
    }

    @Test
    public void shouldCreateUserWithEmptyName() {
        User user = User.builder()
                .login("login1")
                .email("mail1@mail.ru")
                .birthday(LocalDate.of(2003, 6, 8))
                .build();

        controller.createUser(user);

        assertEquals("login1", user.getName());
    }

}

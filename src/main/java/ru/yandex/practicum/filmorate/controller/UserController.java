package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")

public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        try {
            validateUser(user);
            user.setId(getNextId());
            users.put(user.getId(), user);
            log.info("Пользователь успешно создан: {}", user);
            return user;
        } catch (ValidationException e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        }

    }

    private  void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw  new ValidationException("Электронная почта не может быть пустой и должна содержать знак - @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        LocalDate toDay = LocalDate.now();
        if (user.getBirthday() == null || user.getBirthday().isAfter(toDay)) {
            throw new ValidationException("Дата рождения не может быть больше текущей даты");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        try {
            if (user.getId() == 0) {
                throw new ValidationException("ID не может быть равен 0");
            }
            long id = user.getId();

            if (users.containsKey(id)) {
                validateUser(user);
                user.setId(id);
                users.put(id, user);
                log.info("Пользователь обновлен: {}", user);
            } else {
                throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден.");
            }
        } catch (ValidationException e) {
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        } catch (NotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e; // Перебрасываем исключение, чтобы клиент мог обработать его
        }
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

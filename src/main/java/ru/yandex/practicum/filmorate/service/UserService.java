package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserStorage userStorage;

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == 0) {
            throw new ValidationException("ID не может быть равен 0");
        }
        return userStorage.update(user);
    }

    public User getById(Long userId) {
        return userStorage.getById(userId);
    }

    public Collection<User> getAllValues() {
        return userStorage.getAllValues();
    }

    public Map<Long, User> getAll() {
        return userStorage.getAll();
    }

    public void delete(Long id) {
        userStorage.deleteById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        return userStorage.getMutualFriends(userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        return userStorage.getAllFriends(userId);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать знак - @");
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
}

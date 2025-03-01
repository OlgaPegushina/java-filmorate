package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        log.debug("UserService({}).", userStorage.getClass().getSimpleName());
        this.userStorage = userStorage;
        log.info("Подключена зависимость: {}.", userStorage.getClass().getName());
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
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
        User user = getById(userId);
        User friend = getById(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
            log.info("Пользователи {} и {} теперь друзья", user, friend);
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователи {} и {} уже являются друзьями", user, friend);
        }
        if (Objects.equals(userId, friendId)) {
            log.warn("Друг не добавлен: {}.", userId);
            throw new ValidationException("Id друзей совпадают");
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        /*if (!user.getFriends().contains(friendId) || !friend.getFriends().contains(userId)) {
            throw new ValidationException("Пользователи не являются друзьями " + user +" " + friend);
        }*/
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} теперь не являются друзьями", user, friend);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        Set<Long> mutualFriendsIds = new HashSet<>(user.getFriends());
        mutualFriendsIds.retainAll(friend.getFriends());
        return mutualFriendsIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getAllFriends(Long userId) {
        User user = getById(userId);
        return user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}

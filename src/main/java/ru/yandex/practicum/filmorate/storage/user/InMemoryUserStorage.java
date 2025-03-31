package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idUser = 0;

    @Override
    public User create(User user) {
        idUser++;
        user.setId(idUser);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: {}", user);
        return user;
    }

    @Override
    public User getById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
    }

    @Override
    public Map<Long, User> getAll() {
        return users;
    }

    @Override
    public Collection<User> getAllValues() {
        return users.values();
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        } else {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден.");
        }
        return user;
    }

    @Override
    public void deleteById(Long id) {
        if (users.containsKey(id)) {
            users.remove(id);
        } else {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
    }


    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
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

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> getMutualFriends(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        Set<Long> mutualFriendsIds = new HashSet<>(user.getFriends());
        mutualFriendsIds.retainAll(friend.getFriends());
        return mutualFriendsIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        User user = getById(userId);
        return user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}

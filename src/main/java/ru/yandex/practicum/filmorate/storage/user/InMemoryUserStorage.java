package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
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
}

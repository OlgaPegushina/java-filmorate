package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.mpa.MpaRepository;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserStorage userStorage;
    FilmStorage filmStorage;
    MpaRepository mpaRepository;
    FeedStorage feedStorage;

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
        return userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", userId)));
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
        feedStorage.addEvent(userId, friendId, EventOperation.ADD, EventType.FRIEND);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
        feedStorage.addEvent(userId, friendId, EventOperation.REMOVE, EventType.FRIEND);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        return userStorage.getMutualFriends(userId, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        return userStorage.getAllFriends(userId);
    }

    public List<Film> getRecommendations(Long userId) {
        userStorage.validateUser(userId);

        Set<Long> likedFilms = filmStorage.getLikedFilmIdsByUser(userId);
        if (likedFilms.isEmpty()) return List.of();

        Map<Long, Integer> similarUsers = filmStorage.getUsersWithCommonLikes(userId, likedFilms);
        if (similarUsers.isEmpty()) return List.of();

        List<Film> recommendedFilms = filmStorage.findRecommendedFilmsForUser(similarUsers.keySet(), likedFilms);

        for (Film film : recommendedFilms) {
            film.setLikes(filmStorage.getLikeUserIds(film.getId()));
            film.setGenres(filmStorage.getGenre(film.getId()));
            film.setMpa(mpaRepository.getMpaById(film.getMpa().getId()));
        }

        return recommendedFilms;
    }

    public Collection<Feed> getFeed(Long userId) {
        getById(userId);
        return feedStorage.getFeed(userId);
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

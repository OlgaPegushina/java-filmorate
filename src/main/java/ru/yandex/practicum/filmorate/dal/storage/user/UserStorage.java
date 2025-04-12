package ru.yandex.practicum.filmorate.dal.storage.user;

import ru.yandex.practicum.filmorate.dal.storage.Storage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage extends Storage<User> {
    Set<Long> getFriendIds(long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getMutualFriends(Long userId, Long friendId);

    List<User> getAllFriends(Long userId);

    void validateUser(Long userId);
}

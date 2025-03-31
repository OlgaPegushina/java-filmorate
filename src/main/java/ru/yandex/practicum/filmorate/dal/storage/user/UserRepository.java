package ru.yandex.practicum.filmorate.dal.storage.user;

import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;

public interface UserRepository extends UserStorage {
    Set<Long> getFriendIdsFromDB(long id);
}

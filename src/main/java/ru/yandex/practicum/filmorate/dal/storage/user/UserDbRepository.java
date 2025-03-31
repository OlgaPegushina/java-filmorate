package ru.yandex.practicum.filmorate.dal.storage.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FriendIdsRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("userDbRepository")
@Primary // Устанавливаем UserDbRepository как первичный бин
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDbRepository extends BaseRepository<User> implements UserRepository {
    RowMapper<User> mapper = new UserRowMapper();

    public UserDbRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public User create(User user) {
        String query = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        long id = super.create(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );

        user.setId(id);
        user.setFriends(new HashSet<>());
        return user;
    }

    @Override
    public User getById(Long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        User user = findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", id)));
        Set<Long> friends = getFriendIdsFromDB(id);
        user.setFriends(friends);
        return user;
    }

    @Override
    public Map<Long, User> getAll() {
        return getAllValues().stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    @Override
    public List<User> getAllValues() {
        String query = "SELECT * FROM users";
        List<User> users = findMany(query, mapper);
        for (User user : users) {
            user.setFriends(getFriendIdsFromDB(user.getId()));
        }
        return users;
    }

    @Override
    public User update(User user) {
        String query = "UPDATE users SET email = ?, login = ?, name = ?,  birthday = ?  WHERE user_id = ?";
        super.update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM users WHERE user_id = ?";
        boolean isDeleted = super.delete(query, id);

        if (!isDeleted) {
            throw new InternalServerException(String.format("Не удалось удалить пользователя с id: %d.", id));
        }
    }

    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        User user = findOne(query, mapper, email)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с email %s не найден.", email)));
        Set<Long> friends = getFriendIdsFromDB(user.getId());
        user.setFriends(friends);
        return user;
    }


    @Override
    public void addFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);
        String query = "INSERT INTO friendship_status (user_id, friend_id, is_status) VALUES (?, ?, ?)";
        super.update(query, userId, friendId, false);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);
        String query = "DELETE FROM friendship_status where user_id = ? and friend_id = ?";
        super.delete(query, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIdsFromDB(long id) {
        String query = "SELECT friend_id FROM friendship_status WHERE user_id = ?";
        return new HashSet<>(jdbc.query(query, new FriendIdsRowMapper(), id));
    }

    @Override
    public List<User> getMutualFriends(Long userId1, Long userId2) {
        User user1 = getById(userId1);
        User user2 = getById(userId2);
        String query = "SELECT u.* " +
                "FROM users AS u " +
                "INNER JOIN friendship_status f1 ON u.user_id = f1.friend_id " +
                "INNER JOIN friendship_status f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? " +
                "    AND f2.user_id = ?;";
        return findMany(query, mapper, userId1, userId2);
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        getById(userId);
        String query = "SELECT * FROM users u WHERE u.user_id IN " +
                "(SELECT f.friend_id FROM friendship_status f WHERE f.user_id = ?)";
        return findMany(query, mapper, userId);
    }
}

package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.dal.storage.user.UserDbRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbRepository.class})
@ContextConfiguration(classes = {FilmorateApplication.class})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserDbRepositoryTest {
    UserDbRepository userDbRepository;


    @Test
    void createTest() {
        User user = createUser();
        User newUser = userDbRepository.create(user);

        assertThat(newUser).hasFieldOrPropertyWithValue("id", newUser.getId());
        assertThat(newUser).hasFieldOrPropertyWithValue("name", user.getName());
        assertThat(newUser).hasFieldOrPropertyWithValue("login", user.getLogin());
        assertThat(newUser).hasFieldOrPropertyWithValue("birthday", user.getBirthday());
        assertThat(newUser).hasFieldOrPropertyWithValue("email", user.getEmail());

        assertThat(newUser).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(newUser).hasFieldOrPropertyWithValue("name", "Имя");
        assertThat(newUser).hasFieldOrPropertyWithValue("login", "логин");
        assertThat(newUser).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1999, 9, 9));
        assertThat(newUser).hasFieldOrPropertyWithValue("email", "email@mail.ru");
    }

    private User createUser() {
        return User
                .builder()
                .name("Имя")
                .login("логин")
                .birthday(LocalDate.of(1999, 9, 9))
                .email("email@mail.ru")
                .build();
    }

    @Test
    void getByIdTest() {
        User user = createUser();
        User newUser = userDbRepository.create(user);
        User userById = userDbRepository.getById(newUser.getId());

        assertThat(userById).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(userById).hasFieldOrPropertyWithValue("name", "Имя");
        assertThat(userById).hasFieldOrPropertyWithValue("login", "логин");
        assertThat(userById).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1999, 9, 9));
        assertThat(userById).hasFieldOrPropertyWithValue("email", "email@mail.ru");
    }

    @Test
    void getAllTest() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        Map<Long, User> collection = userDbRepository.getAll();

        assertEquals(collection.size(), 2, "Количество возвращено неверно");
        assertEquals(collection.get(1L), user1, "user1 возвращается неверно");
        assertEquals(collection.get(2L), user2, "user2 возвращается неверно");
    }

    @Test
    void getAllValuesTest() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        List<User> collection = userDbRepository.getAllValues();

        assertEquals(collection.size(), 2, "Количество возвращено неверно");
        assertEquals(collection.get(0), user1, "user1 возвращается неверно");
        assertEquals(collection.get(1), user2, "user2 возвращается неверно");
    }

    @Test
    void updateTest() {
        User user = userDbRepository.create(createUser());
        User userUpdate = User.builder()
                .id(user.getId())
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.update(userUpdate);
        user = userDbRepository.getById(1L);

        assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(user).hasFieldOrPropertyWithValue("name", "Имя2");
        assertThat(user).hasFieldOrPropertyWithValue("login", "логин2");
        assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 8, 19));
        assertThat(user).hasFieldOrPropertyWithValue("email", "email2@mail.ru");
    }

    @Test
    void deleteByIdTest() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        userDbRepository.deleteById(1L);
        Map<Long, User> collection = userDbRepository.getAll();

        assertEquals(collection.size(), 1, "Количество возвращено неверно");
        assertEquals(collection.get(2L), user2, "user2 возвращается неверно");
    }

    @Test
    void addFriendTest() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        userDbRepository.addFriend(user1.getId(), user2.getId());

        Set<Long> friends = userDbRepository.getFriendIds(user1.getId());

        assertEquals(friends.size(), 1, "Количество друзей возвращается неверно");
        assertTrue(friends.contains(user2.getId()));
    }

    @Test
    void removeFriendTest() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        User user3 = User.builder()
                .login("логин3")
                .name("Имя3")
                .email("email3@mail.ru")
                .birthday(LocalDate.of(2000, 10, 19))
                .build();
        userDbRepository.create(user3);

        userDbRepository.addFriend(user1.getId(), user2.getId());
        userDbRepository.addFriend(user1.getId(), user3.getId());

        userDbRepository.removeFriend(user1.getId(), user2.getId());

        Set<Long> friends = userDbRepository.getFriendIds(user1.getId());

        assertEquals(friends.size(), 1, "Количество друзей возвращается неверно");
        assertTrue(friends.contains(user3.getId()));
    }

    @Test
    void getFriendIds() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        User user3 = User.builder()
                .login("логин3")
                .name("Имя3")
                .email("email3@mail.ru")
                .birthday(LocalDate.of(2000, 10, 19))
                .build();
        userDbRepository.create(user3);

        userDbRepository.addFriend(user1.getId(), user2.getId());
        userDbRepository.addFriend(user1.getId(), user3.getId());

        Set<Long> friends = userDbRepository.getFriendIds(user1.getId());

        assertEquals(friends.size(), 2, "Количество друзей возвращается неверно");
        assertTrue(friends.contains(user2.getId()));
        assertTrue(friends.contains(user3.getId()));
    }

    @Test
    void getMutualFriends() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        User user3 = User.builder()
                .login("логин3")
                .name("Имя3")
                .email("email3@mail.ru")
                .birthday(LocalDate.of(2000, 10, 19))
                .build();
        userDbRepository.create(user3);

        userDbRepository.addFriend(user1.getId(), user2.getId());
        userDbRepository.addFriend(user2.getId(), user1.getId());
        userDbRepository.addFriend(user3.getId(), user2.getId());

        List<User> friends = userDbRepository.getMutualFriends(user1.getId(), user3.getId());

        assertEquals(friends.size(), 1, "Количество друзей возвращается неверно");
        assertTrue(friends.contains(user2));

        friends = userDbRepository.getMutualFriends(user2.getId(), user3.getId());

        assertEquals(friends.size(), 0, "Количество друзей возвращается неверно");
    }

    @Test
    void getAllFriends() {
        User user1 = userDbRepository.create(createUser());
        User user2 = User.builder()
                .login("логин2")
                .name("Имя2")
                .email("email2@mail.ru")
                .birthday(LocalDate.of(2000, 8, 19))
                .build();
        userDbRepository.create(user2);
        User user3 = User.builder()
                .login("логин3")
                .name("Имя3")
                .email("email3@mail.ru")
                .birthday(LocalDate.of(2000, 10, 19))
                .build();
        userDbRepository.create(user3);

        userDbRepository.addFriend(user1.getId(), user2.getId());
        userDbRepository.addFriend(user1.getId(), user3.getId());

        Set<Long> friends = userDbRepository.getFriendIds(user1.getId());

        assertEquals(friends.size(), 2, "Количество друзей возвращается неверно");
        assertTrue(friends.contains(user2.getId()));
        assertTrue(friends.contains(user3.getId()));
    }
}

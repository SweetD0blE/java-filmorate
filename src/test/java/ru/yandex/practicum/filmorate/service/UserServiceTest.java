package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {
    private final UserService userService;

    User user = User.builder()
            .login("login")
            .email("email@ya.ru")
            .birthday(LocalDate.now())
            .build();

    @Test
    public void testCreateUserMustReturnUserWithNewId() {
        User userTest = User.builder()
                .id(-1)
                .login("test")
                .email("test@ya.ru")
                .birthday(LocalDate.now())
                .build();
        User expectUser = userService.create(userTest);

        assertNotEquals(-1, expectUser.getId(), "Пользователь не записан в БД, id не был присвоен");
    }

    @Test
    public void testCreateUserSimilar() {
        user.setLogin("testCreateUserSimilar");
        int id = userService.create(user).getId();
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> userService.create(user)
        );

        assertEquals("Пользователь с одинаковыми параметрами уже существует. Его id =" + id, exception.getMessage());
    }

    @Test
    public void testFindUserByWrongId() {
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> userService.getUser(-1)
        );

        assertEquals("Пользователя с указанным id не существует", exception.getMessage());
    }

    @Test
    public void testUpdateUserWithWrongId() {
        User userTest = User.builder()
                .id(-1)
                .login("testUpdateUserWithWrongId")
                .email("testUpdate@ya.ru")
                .birthday(LocalDate.now())
                .build();
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> userService.update(userTest)
        );

        assertEquals("Пользователь с идентификатором -1 не найден.", exception.getMessage());
    }

    @Test
    public void testAddFriendWithWrongId() {
        user.setLogin("addFriend");
        User userTest = userService.create(user);
        StorageException exception = assertThrows(
                StorageException.class,
                () -> userService.addFriend(userTest.getId(), -1)
        );

        assertEquals("Пользователя с указанным id не существует", exception.getMessage());

        exception = assertThrows(
                StorageException.class,
                () -> userService.addFriend(-1, userTest.getId())
        );

        assertEquals("Пользователя с указанным id не существует", exception.getMessage());
    }

    @Test
    public void testAddFriend() {
        user.setLogin("testAddFriend");
        user = userService.create(user);
        User userFriend = User.builder()
                .id(-1)
                .login("testAddFriend")
                .email("testAddFriend@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userFriend = userService.create(userFriend);
        user = userService.addFriend(user.getId(), userFriend.getId());
        userFriend = userService.getUser(userFriend.getId());

        assertEquals(1, user.getFriends().size(), "Дружба не записана для user");
        assertTrue(userFriend.getFriends().isEmpty(), "Дружба записана для friend");

        userFriend = userService.addFriend(userFriend.getId(), user.getId());

        assertEquals(1, userFriend.getFriends().size(), "Дружба не записана для friend");
    }

    @Test
    public void testDeleteFriend() {
        user.setLogin("testDeleteFriend");
        user = userService.create(user);
        User userFriend = User.builder()
                .id(-1)
                .login("testDeleteFriend")
                .email("testDeleteFriend@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userFriend = userService.create(userFriend);
        user = userService.addFriend(user.getId(), userFriend.getId());
        userFriend = userService.addFriend(userFriend.getId(), user.getId());

        assertEquals(1, userFriend.getFriends().size(), "Дружба не записана для friend");

        userFriend = userService.deleteFriend(userFriend.getId(), user.getId());
        user = userService.getUser(user.getId());

        assertTrue(userFriend.getFriends().isEmpty(), "Дружба записана для friend");
    }

    @Test
    public void testGetCommonFriends() {
        user.setLogin("testGetCommonFriends");

        user = userService.create(user);
        User userFriend = User.builder()
                .id(-1)
                .login("testGetCommonFriends")
                .email("testCreate@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userFriend = userService.create(userFriend);

        User userFriend2 = User.builder()
                .id(-1)
                .login("testGetCommonFriends")
                .email("testGetCommonFriends@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userFriend2 = userService.create(userFriend2);

        user = userService.addFriend(user.getId(), userFriend2.getId());
        userFriend = userService.addFriend(userFriend.getId(), userFriend2.getId());

        List<User> commonFriends = userService.getCommonFriends(user.getId(), userFriend.getId());


        assertEquals(1, commonFriends.size(), "Не ищет общих друзей");
    }
}

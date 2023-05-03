package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServiceException;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        findMatch(user);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        user.setId((int) simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue());
        log.info("Создан пользователь: {} {}", user.getId(), user.getName());
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, user.getId());

        if (filmRows.next()) {
            List<Integer> friends = user.getFriends();
            user.setFriends(updateFriends(friends, user.getId()));

            sqlQuery = "UPDATE users " +
                    "SET email = ?, " +
                    "login = ?, " +
                    "name = ?, " +
                    "birthday = ?;";

            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday());

            log.info("Обновлен пользователь: {} {}", user.getId(), user.getName());
            return user;
        } else {
            log.warn("Пользователь с идентификатором {} не найден.", user.getId());
            throw new StorageException(String.format("Пользователь с идентификатором %d не найден.", user.getId()));
        }
    }

    @Override
    public void delete(User user) {
        String sqlQuery = "DELETE FROM users " +
                "WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, user.getId());
        log.info("Пользователь {} был удален.", user.getId());
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM users");

        while (filmsRows.next()) {
            users.add(findById(Integer.parseInt(filmsRows.getString("user_id"))));
        }

        log.info("Всего пользователей: {}.",users.size());
        return users;
    }

    @Override
    public User findById(int userId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", userId);
        if (userRows.next()) {
            SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(
                    "SELECT friend_id " +
                            "FROM user_friends " +
                            "WHERE (user_id = ?);", userId);

            List<Integer> friends = new ArrayList<>();
            while (friendsRows.next()) {
                friends.add(Integer.parseInt(friendsRows.getString("friend_id")));
            }

            User user = new User(
                    Integer.parseInt(userRows.getString("user_id")),
                    userRows.getString("email").trim(),
                    userRows.getString("login").trim(),
                    userRows.getString("name").trim(),
                    LocalDate.parse(userRows.getString("birthday").trim(), formatter),
                    friends);

            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
            return user;
        } else {
            log.warn("Пользователь с идентификатором {} не был найден.", userId);
            throw new StorageException("Пользователя с указанным id не существует");
        }
    }

    @Override
    public User addFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);
        String sqlQuery = "SELECT * FROM user_friends " +
                "WHERE user_id = ? " +
                "AND friend_id = ?;";
        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(sqlQuery, userId, friendId);
        if (friendsRows.next()) {
            boolean isConfirm = friendsRows.getBoolean("status");
            if (isConfirm) {
                log.info("Вы уже дружите с пользователем {} ", friendId);
                throw new ServiceException(String.format("Вы уже дружите с пользователем %s", friendId));
            } else {
                log.warn("Пользователю {} отправлен запрос на дружбу. Ожидание подтверждения", friendId);
                throw new ServiceException(String.format("Пользователю %s отправлен запрос на дружбу." +
                        " Ожидание подтверждения", friendId));
            }
        }
        friendsRows = jdbcTemplate.queryForRowSet(sqlQuery, friendId,userId);
        if (friendsRows.next()) {
            sqlQuery = "UPDATE friendships " +
                    "SET status = true " +
                    "WHERE user_id = ? AND friend_id = ?;";
            jdbcTemplate.update(sqlQuery, friendId, userId);
            sqlQuery = "INSERT INTO friendships (user_id, friend_id, status) " +
                    "VALUES (?, ?, ?);";
            jdbcTemplate.update(sqlQuery, userId, friendId, true);

            log.info("Подтвержден запрос пользователя {} пользователю {} на дружбу.", friendId, userId);
            return findById(userId);
        }

        sqlQuery = "INSERT INTO friendships (user_id, friend_id) " +
                "VALUES (?, ?);";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Пользователь {} отправил запрос на дружбу пользователю {}.", userId, friendId);
        return findById(userId);
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friendships " +
                "WHERE user_id = ? " +
                "AND friend_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        sqlQuery = "UPDATE friendships " +
                "SET status = false " +
                "WHERE user_id = ? AND friend_id = ?;";

        log.info("Пользователь {} удалил из списка друзей пользователя {}.", userId, friendId);
        return findById(userId);
    }

    private List<Integer> updateFriends(List<Integer> friends, int userId) {
        String sqlQuery = "DELETE FROM friendships " +
                "WHERE user_id = ?;";

        jdbcTemplate.update(sqlQuery, userId);

        if (friends == null || friends.isEmpty()) {
            return friends;
        }
        friends = friends.stream()
                .distinct().collect(Collectors.toList());
        for (Integer friend : friends) {
            sqlQuery = "INSERT INTO friendships (user_id, friend_id) " +
                    "VALUES (?, ?);";

            jdbcTemplate.update(sqlQuery, userId, friend);
        }
        return friends;
    }

    private void findMatch(User user) {
        String sqlQuery = "SELECT * FROM users " +
                "WHERE (email=?) AND (login=?) AND (birthday=?);";
        SqlRowSet usersRow = jdbcTemplate.queryForRowSet(sqlQuery, user.getEmail(), user.getLogin(), user.getBirthday());

        if (usersRow.next()) {
            int id = Integer.parseInt(usersRow.getString("user_id"));
            throw new StorageException(String.format("Пользователь с похожими параметрами уже существует, id = %d", id));
        }
    }
}

package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.ServiceException;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        findMatch(user);
        String sqlQuery = "INSERT INTO users(email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "update users set " +
                "email= ?, login = ?, name = ?, birthday = ? " +
                "where user_id = ?";
        int res = jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(),
                user.getName(),user.getBirthday(), user.getId());
        if (res < 1) throw new StorageException("Невозможно обновить id " + user.getId() +
                " .Его нет в БД");
        return user;
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
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::makeUser);
    }

    @Override
    public User findById(int userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, userId);
        if (userRows.next()) {
            log.info("Найден пользователь: {} {}", userRows.getString("user_id"), userRows.getString("login"));
            return jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
        } else {
            log.warn("Пользователь с идентификатором {} не найден.", userId);
            throw new StorageException("Такого пользователя не существует");

        }
    }

    @Override
    public List<User> getFriends(int id) {
        String sqlQuery = "SELECT users.user_id, email, login, name, birthday " +
                "FROM users " +
                "LEFT JOIN friendships AS frndshps ON users.user_id = frndshps.friend_id " +
                "WHERE frndshps.user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::makeUser, id);
    }

    @Override
    public User addFriend(int userId, int friendId) {
        String sqlQuery = "MERGE INTO friendships (user_id, friend_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        return findById(userId);
    }


    @Override
    public User deleteFriend(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friendships " +
                "WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        return findById(userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        String sqlQuery = "SELECT users.user_id, email, login, name, birthday " +
                "FROM friendships AS frndshp " +
                "LEFT JOIN users ON users.user_id = frndshp.friend_id " +
                "WHERE frndshp.user_id = ? AND frndshp.friend_id IN ( " +
                "SELECT friend_id " +
                "FROM friendships AS frndshp " +
                "LEFT JOIN users ON users.user_id = frndshp.friend_id " +
                "WHERE frndshp.user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::makeUser, userId, friendId);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }


    private void findMatch(User user) {
        String sqlQuery = "SELECT * FROM users " +
                "WHERE (email=?) AND (login=?) AND (birthday=?);";
        SqlRowSet usersRow = jdbcTemplate.queryForRowSet(sqlQuery, user.getEmail(), user.getLogin(), user.getBirthday());

        if (usersRow.next()) {
            int id = Integer.parseInt(usersRow.getString("user_id"));
            throw new StorageException(String.format("Пользователь с одинаковыми параметрами уже существует. Его id = %d", id));
        }
    }
}

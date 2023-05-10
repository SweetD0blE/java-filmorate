package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    void delete(User user);

    List<User> findAll();

    List<User> getFriends(int id);

    List<User> getCommonFriends(int userId, int friendId);

    User findById(int userId);

    User addFriend(int userId, int friendId);

    User deleteFriend(int userId, int friendId);
}

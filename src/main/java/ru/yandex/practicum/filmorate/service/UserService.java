package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.ServiceException;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        checkUserName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        checkUserName(user);
        return userStorage.update(user);
    }

   public List<User> getUsers() {
        return userStorage.findAll();
   }

   public User getUser(int id) {
        return userStorage.findById(id);
   }

   public User addFriend(int id, int idFriend) {
        if (id == idFriend || userStorage.findById(id) == null || userStorage.findById(idFriend) == null) {
            throw new ServiceException("Невозможно добавить друга. Ваше id= " + id + ". Id друга= " + idFriend);
        }
       return userStorage.addFriend(id, idFriend);
   }

   public User deleteFriend(int id, int idFriend) {
        return userStorage.deleteFriend(id, idFriend);
   }

   public List<User> getFriends(int id) {
        return userStorage.getFriends(id);
   }

    public List<User> getCommonFriends(int userId, int friendId) {
        return userStorage.getCommonFriends(userId, friendId);
    }

    private void checkUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !(user.getEmail().contains("@"))) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }
}

package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServiceException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
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
        return userStorage.getUsers();
   }

   public User getUser(int id) {
        return userStorage.getUser(id);
   }

   public User addFriend(int id, int idFriend) {
        if (id == idFriend) {
            throw new ServiceException("Невозможно добавить в друзья самого себя. У вас одинаковые ID.");
        }
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(idFriend);

       Set<Integer> friends = user.getFriends();
       friends.add(idFriend);
       user.setFriends(friends);

       friends = friend.getFriends();
       friends.add(id);
       friend.setFriends(friends);
       update(friend);

       return update(user);
   }

   public User deleteFriend(int id, int idFriend) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(idFriend);

        Set<Integer> friends = user.getFriends();
        friends.remove(idFriend);
        user.setFriends(friends);

        friends = friend.getFriends();
        friends.remove(id);
        friend.setFriends(friends);
        update(friend);

        return update(user);
   }

   public List<User> getFriends(int id) {
        User user = userStorage.getUser(id);
        List<User> friends = new ArrayList<>();

        Set<Integer> idFriends = user.getFriends();

        for (Integer i : idFriends) {
            friends.add(userStorage.getUser(i));
        }
        return friends;
   }

   public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> friendsUser = new HashSet<>(userStorage.getUser(id).getFriends());
        Set<Integer> friendsOtherUser = new HashSet<>(userStorage.getUser(otherId).getFriends());
        List<User> commonFriends = new ArrayList<>();
        friendsUser.retainAll(friendsOtherUser);

        for (Integer i : friendsUser) {
            commonFriends.add(userStorage.getUser(i));
        }
        return commonFriends;
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

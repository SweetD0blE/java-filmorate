package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    @Override
    public User create(User user) {
        user.setId(id);
        users.put(user.getId(), user);
        id++;
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new StorageException("Пользователя с id=" + user.getId() + " не существует");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User delete(User user) {
        if (!users.containsKey(user.getId())) {
            throw new StorageException("Пользователя с id=" + user.getId() + " не существует");
        }
        users.remove(user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public User getUser(int id) {
        if (!users.containsKey(id)) {
            throw new StorageException("Пользователя с id=" + id + " не существует");
        }
        return users.get(id);
    }
}

package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UpdateException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private int id;
    {
        id = 1;
    }

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        checkUserName(user);
        user.setId(id);
        log.info("Создан пользователь с id: {}", user.getId());
        users.put(user.getId(), user);
        id++;
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        checkUserName(user);
        if (users.containsKey(user.getId())) {
            log.info("Обновлены данные пользователя с id: {}", user.getId());
            users.put(user.getId(), user);
            return user;
        } else {
            throw new UpdateException("Пользователя с id=" + user.getId() + " не существует");
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.info("Пользователю с id = {}, установлено имя: {}", user.getId(), user.getName());
        }
        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Пользователю с id = {}, установлено имя: {}", user.getId(), user.getName());
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !(user.getEmail().contains("@"))) {
            log.warn("Ошибка валидации: Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }
}

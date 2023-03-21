package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UpdateException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private int id;

    {
        id = 1;
    }

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        checkDate(film);
        film.setId(id);
        log.info("Создан фильм с id: {}", film.getId());
        films.put(film.getId(), film);
        id++;
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        checkDate(film);
        if (films.containsKey(film.getId())) {
            log.info("Обновлены данные по фильму с id: {}", film.getId());
            films.put(film.getId(), film);
            return film;
        } else {
            throw new UpdateException("Фильма с id=" + film.getId() + " не существует");
        }
    }

    private void checkDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма не раньше 28 декабря 1895 года.");
        }
    }
}

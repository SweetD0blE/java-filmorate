package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    @Override
    public Film create(Film film) {
        film.setId(id);
        films.put(film.getId(), film);
        id++;
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        } else {
            throw new StorageException("Фильма с id=" + film.getId() + " не существует");
        }
    }

    @Override
    public Film delete(Film film) {
       if (films.containsKey(film.getId())) {
           films.remove(film.getId());
           return film;
       } else {
           throw new StorageException("Фильма с id=" + film.getId() + " не существует");
       }
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<Film>(films.values());
    }

    @Override
    public Film getFilm(int id) {
       if (films.containsKey(id)) {
           return films.get(id);
       } else {
           throw new StorageException("Пользователя с d=" + getFilm(id) + " не существует");
       }
    }
}

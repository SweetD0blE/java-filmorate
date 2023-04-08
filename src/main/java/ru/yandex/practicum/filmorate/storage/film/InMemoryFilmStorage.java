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
        if (!films.containsKey(film.getId())) {
            throw new StorageException("Фильма с id=" + film.getId() + " не существует");
        }
        films.put(film.getId(), film);
        return film;

    }

    @Override
    public Film delete(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new StorageException("Фильма с id=" + film.getId() + " не существует");
        }
        films.remove(film.getId());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<Film>(films.values());
    }

    @Override
    public Film getFilm(int id) {
       if (!films.containsKey(id)) {
           throw new StorageException("Фильма с id=" + id + " не существует");
       }
        return films.get(id);
    }
}

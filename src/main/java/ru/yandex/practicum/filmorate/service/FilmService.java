package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        check(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        check(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film getFilm(int id) {
        return filmStorage.findById(id);
    }

    public Film like(int filmId, int userId) {
        userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        List<Integer> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);
        update(film);
        return film;
    }

    public Film removeLike(int filmId, int userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getTopCountPopularFilms(int count) {
        return getFilms().stream()
                .sorted(Comparator.comparing(Film::getAmountFilmLikes).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void check(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма не раньше 28 декабря 1895 года.");
        }
    }
}

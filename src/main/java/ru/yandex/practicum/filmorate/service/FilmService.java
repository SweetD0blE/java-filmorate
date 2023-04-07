package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        checkDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        checkDate(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public Film like(int filmId, int userId) {
        Film film = filmStorage.getFilm(filmId);
        Set<Integer> likes = film.getLikes();
        likes.add(userId);
        return film;
    }

    public Film removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilm(filmId);
        Set<Integer> likes = film.getLikes();
        boolean isRemove = likes.remove(userId);
        if (!isRemove || userId <= 0) {
            throw new StorageException("Лайк пользователя" + userId + " не был найден");
        }
        return film;
    }

    public List<Film> getTop10PopularFilms(int count) {
        List<Film> films = filmStorage.getFilms();
        Comparator<Film> filmComparator = Comparator.comparingInt(o -> o.getLikes().size());
        Collections.sort(films, filmComparator.reversed());

        if (count >= films.size()) {
            return films;
        }
        return films.subList(0, count);
    }

    private void checkDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма не раньше 28 декабря 1895 года.");
        }
    }


}

package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        check(film);
        return filmStorage.create(film);
    }

    public Film delete(Film film) {
        check(film);
        return filmStorage.delete(film);
    }

    public Film update(Film film) {
        check(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
       return filmStorage.findAll();
    }

    public Film getFilm(int id) {
        Film film = filmStorage.findById(id);
        if (film == null) {
            throw new StorageException("Фильм с id = " + id + " не найден.");
        }
        return film;
    }

    public Film addLike(int filmId, int userId) {
        checkUserId(userId);
        checkFilmId(filmId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(int filmId, int userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getTopCountPopularFilms(int count) {
        List<Film> films = filmStorage.findAll();
        if (count > films.size()) {
            count = films.size();
        }
        return films.stream()
                .sorted((p0, p1) -> {
                    int comp = compare(p0.getMpa().getId(), p1.getMpa().getId());
                    return -1 * comp;
                }).limit(count)
                .collect(Collectors.toList());
    }

    private void check(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза фильма не раньше 28 декабря 1895 года.");
        }

        if ((film.getMpa() != null) && (film.getMpa().getId() == 0)) {
            throw new ValidationException("Получены некорректные данные. Не указан id рейтинга фильма.");
        }

        if (film.getGenres() != null) {
            Set<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                if (genre.getId() == 0) {
                    throw new ValidationException(
                            String.format(
                                    "Получены некорректные данные. Не указан id жанра %s фильма.", genre.getName()));
                }
            }
        }
    }

    private void checkUserId(int userId) {
        User user = userStorage.findById(userId);
        if (user == null) {
            throw new StorageException("Пользователь не найден");
        }
    }

    private void checkFilmId(int filmId) {
        Film film = filmStorage.findById(filmId);
        if (film == null) {
            throw new StorageException("Фильм не найден");
        }
    }
}

package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Film delete(Film film);

    List<Film> findAll();

    Film findById(int id);

    Film removeLike(int filmId, int userId);

    Film addLike(int filmId, int userId);
}

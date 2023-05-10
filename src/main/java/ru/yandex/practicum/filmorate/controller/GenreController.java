package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/genres")
public class GenreController {

    private final GenreDao genreDao;

    public GenreController(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Genre findGenreById(@PathVariable int id) {
        return genreDao.findGenreById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> findAll() {
        return genreDao.findAllGenre();
    }
}

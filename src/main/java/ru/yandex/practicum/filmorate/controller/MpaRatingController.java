package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.MpaRatingDao;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaRatingController {
    private final MpaRatingDao mpaRatingDao;

    public MpaRatingController(MpaRatingDao mpaRatingDao) {
        this.mpaRatingDao = mpaRatingDao;
    }

    @GetMapping
    public List<MpaRating> findAllMpa() {
        return mpaRatingDao.findAllMpa();
    }

    @GetMapping("/{id}")
    public MpaRating findMpaById(@PathVariable int id) {
        return mpaRatingDao.findMpaById(id);
    }
}

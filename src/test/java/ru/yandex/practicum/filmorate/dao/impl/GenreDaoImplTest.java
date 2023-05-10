package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class GenreDaoImplTest {
    private final GenreDaoImpl genreDao;

    @Test
    void findGenreById() {
        Genre genre = genreDao.findGenreById(1);
        assertTrue(genre.getName().equals("Комедия"), "name = Комедия");
    }

    @Test
    void findGenreByWrongId() {
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> genreDao.findGenreById(-1)
        );

        assertEquals("Такого жанра не существует", exception.getMessage());
    }

    @Test
    void getGenres() {
        List<Genre> genres = genreDao.findAllGenre();
        assertTrue(genres.size() == 6, "genre = 6");
    }
}

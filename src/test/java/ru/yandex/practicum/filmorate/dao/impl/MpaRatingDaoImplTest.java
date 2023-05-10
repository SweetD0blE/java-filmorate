package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaRatingDaoImplTest {
    private final MpaRatingDaoImpl mpaRatingDao;

    @Test
    void findRatingById() {
        MpaRating rating = mpaRatingDao.findMpaById(1);
        assertTrue(rating.getName().equals("G"), "name = G");
    }

    @Test
    void findRatingByWrongId() {
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> mpaRatingDao.findMpaById(-1)
        );

        assertEquals("Такого рейтинга не существует", exception.getMessage());
    }

    @Test
    void getRating() {
        List<MpaRating> ratings = mpaRatingDao.findAllMpa();
        assertTrue(ratings.size() == 5, "rating = 5");
    }
}

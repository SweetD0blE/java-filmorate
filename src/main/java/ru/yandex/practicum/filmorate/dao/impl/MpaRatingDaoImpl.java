package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaRatingDao;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MpaRatingDaoImpl implements MpaRatingDao {
    private final JdbcTemplate jdbcTemplate;

    public MpaRatingDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MpaRating findMpaById(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM rating WHERE rating_id = ?", id);
        if (mpaRows.next()) {
            MpaRating mpa = MpaRating.builder()
                    .id(Integer.parseInt(mpaRows.getString("RATING_ID")))
                    .name(mpaRows.getString("NAME").trim())
                    .build();

            log.info("Найден рейтинг: {} {}", mpa.getId(), mpa.getName());
            return mpa;
        } else {
            log.info("Рейтинг с идентификатором {} не найден.", id);
            throw new StorageException(String.format("Рейтинг с идентификатором %d не найден.", id));
        }
    }

    @Override
    public List<MpaRating> getMpa() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM rating");
        List<MpaRating> mpaRatings = new ArrayList<>();

        while (mpaRows.next()) {
            MpaRating mpa = MpaRating.builder()
                    .id(Integer.parseInt(mpaRows.getString("RATING_ID")))
                    .name(mpaRows.getString("NAME").trim())
                    .build();
            mpaRatings.add(mpa);
        }
        log.info("Получен список {} рейтингов", mpaRatings.size());
        return mpaRatings;
    }
}

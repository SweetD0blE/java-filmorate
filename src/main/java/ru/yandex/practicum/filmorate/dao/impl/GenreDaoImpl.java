package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre findGenreById(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM genre WHERE genre_id = ?", id);
        if (genreRows.next()) {
            Genre genre = Genre.builder()
                    .id(Integer.parseInt(genreRows.getString("GENRE_ID")))
                    .name(genreRows.getString("NAME").trim())
                    .build();

            log.info("Найден жанр: {} {}", genre.getId(), genre.getName());
            return genre;
        } else {
            log.info("Жанр с идентификатором {} не найден.", id);
            throw new StorageException(String.format("Жанр с идентификатором %d не найден", id));
        }
    }

    @Override
    public List<Genre> findAll() {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM genre");
        List<Genre> genres = new ArrayList<>();

        while (genreRows.next()) {
            Genre genre = Genre.builder()
                    .id(Integer.parseInt(genreRows.getString("GENRE_ID")))
                    .name(genreRows.getString("NAME").trim())
                    .build();
            genres.add(genre);
        }
        log.info("Получен список из {} жанров", genres.size());
        return genres;
    }
}

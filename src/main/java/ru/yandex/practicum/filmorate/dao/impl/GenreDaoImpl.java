package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre findGenreById(int id) {
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql, id);
        if (userRows.next()) {
            log.info("Найден жанр с идентификатором: {}", userRows.getString("genre_id"));
            return jdbcTemplate.queryForObject(sql, this::makeGenre, id);
        } else {
            log.warn("Жанр с идентификатором {} не найден.", id);
            throw new StorageException("Такого жанра не существует");

        }
    }

    @Override
    public List<Genre> findAllGenre() {
        String sqlQuery = "SELECT * FROM genre";
        return jdbcTemplate.query(sqlQuery, this::makeGenre);
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("GENRE_ID"))
                .name(rs.getString("NAME").trim())
                .build();
    }
}

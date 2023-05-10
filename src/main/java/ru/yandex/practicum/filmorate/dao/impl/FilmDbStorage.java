package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate nmJdbcTemplate;

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films(name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        int generatedId = keyHolder.getKey().intValue();
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        fillGenreTable(film.getGenres().stream().map(Genre::getId).collect(Collectors.toList()), generatedId);
        return findById(generatedId);
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "update films set name = ?, description =?, release_date = ?, duration = ?, rating_id = ?" +
                " where film_id =?";
        jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        String sql3 = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sql3, film.getId());
        fillGenreTable(film.getGenres().stream().map(Genre::getId).collect(Collectors.toList()), film.getId());
        if (findById(film.getId()) == null) {
            throw new StorageException("Фильм с id = " + film.getId() + " не найден.");
        }
        return findById(film.getId());
    }

    @Override
    public Film delete(Film film) {
        String sqlQuery = "DELETE FROM films " +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, film.getId());
        return film;
    }

    @Override
    public List<Film> findAll() {
        String sqlQuery = "SELECT * FROM films f JOIN rating r ON f.rating_id = r.rating_id";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRowToFilm(rs));
        fillGenres(films);
        return films;
    }

    @Override
    public Film findById(int id) {
        String sqlQuery = "SELECT f.*, r.name, r.description AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN rating AS r ON r.rating_id = f.rating_id " +
                "WHERE f.film_id = ? ";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRowToFilm(rs), id);
        //Заполнение жанров
        fillGenres(films);
        return films.isEmpty() ? null : films.get(0);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        String sqlQuery = "DELETE FROM likes " +
                "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return null;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        String sqlQuery = "MERGE INTO likes (film_id, user_id) " +
                "VALUES (?, ?) ";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return null;
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {

        return Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("name").trim())
                .description(rs.getString("description").trim())
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(getRatingById(rs.getInt("rating_id")))
                .likes(Collections.emptySet())
                .genres(Collections.emptySet())
                .build();
    }

    private void fillGenres(List<Film> films) {
        List<Integer> filmsId = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<String, List<Integer>> idsMap = Collections.singletonMap("id", filmsId);
        String genresSql = "SELECT f.film_id, g.genre_id, g.name AS genre_name " +
                "FROM genre AS g " +
                "RIGHT JOIN film_genre AS fg ON g.genre_id = fg.genre_id " +
                "RIGHT JOIN films AS f ON f.film_id = fg.film_id " +
                "WHERE f.film_id IN (:id)";
        List<Map<Integer, TreeSet<Genre>>> genres = nmJdbcTemplate.query(genresSql, idsMap, ((rs, rowNum) -> makeAllGenres(rs)));
        if (!genres.isEmpty()) {
            films.stream()
                    .filter(f -> genres.get(0).containsKey(f.getId()))
                    .forEach(f -> f.setGenres(new HashSet<>(genres.get(0).get(f.getId()))));
        }
    }

    private Map<Integer, TreeSet<Genre>> makeAllGenres(ResultSet rs) throws SQLException {
        Map<Integer, TreeSet<Genre>> genres = new HashMap<>();
        do {
            Integer filmId = rs.getInt("film_id");
            genres.putIfAbsent(filmId, new TreeSet<>(Comparator.comparingLong(Genre::getId)));
            if (rs.getString("name") != null) {
                genres.get(filmId).add(new Genre(rs.getInt("genre_id"), rs.getString("name")));
            }
        } while (rs.next());
        return genres;
    }

    public MpaRating getRatingById(int mpaId) {
        return jdbcTemplate.query("SELECT * FROM rating WHERE rating_id = ?",
                (rs, rowNum) -> new MpaRating(rs.getInt("rating_id"),
                        rs.getString("name"), rs.getString("description")), mpaId).get(0);
    }

    private void fillGenreTable(List<Integer> genresList, int filmId) {
        String genreSql = "INSERT INTO film_genre(film_id, genre_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.batchUpdate(genreSql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, genresList.get(i));
            }

            @Override
            public int getBatchSize() {
                return genresList.size();
            }
        });
    }
}

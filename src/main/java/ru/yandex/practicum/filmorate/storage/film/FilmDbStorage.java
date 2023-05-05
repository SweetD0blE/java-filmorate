package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MpaRatingDao;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingDao mpaRatingDao;
    private final GenreDao genreDao;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaRatingDao mpaRatingDao, GenreDao genreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRatingDao = mpaRatingDao;
        this.genreDao = genreDao;
    }

    @Override
    public Film create(Film film) {
        findMatch(film);
        int mpaId = film.getMpa().getId();
        film.setMpa(mpaRatingDao.findMpaById(mpaId));

        List<Genre> genres = film.getGenres();
        if (genres != null) {
            for (Genre genre : genres) {
                genreDao.findGenreById(genre.getId());
            }
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        film.setId((int) simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue());

        film.setGenres(updateGenres(genres, film.getId()));

        log.info("Создан фильм: {} {}.", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, film.getId());

        if (filmRows.next()) {
            sqlQuery = "UPDATE films " +
                    "SET name= ?, " +
                    "description = ?, " +
                    "release_date = ?, " +
                    "duration = ?, " +
                    "rating_id = ? " +
                    "WHERE film_id = ?;";

            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());

            List<Genre> genres = film.getGenres();
            film.setGenres(updateGenres(genres, film.getId()));

            List<Integer> likes = film.getLikes();
            film.setLikes(updateLikes(likes, film.getId()));

            log.info("Обновлен фильм: {} {}", film.getId(), film.getName());
            return film;
        } else {
            log.warn("Фильм с id = {} не найден", film.getId());
            throw new StorageException(String.format("Фильм с id = %d не найден", film.getId()));
        }
    }

    @Override
    public void delete(Film film) {
        String sqlQuery = "DELETE FROM films " +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, film.getId());
        log.info("Был удален фильм: {} {}", film.getId(), film.getName());
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmsRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films");

        while (filmsRows.next()) {
            films.add(findById(Integer.parseInt(filmsRows.getString("film_id"))));
        }

        Collections.sort(films, Comparator.comparingInt(Film::getId));
        log.info("Найдено фильмов: {}.", films.size());
        return films;
    }

    @Override
    public Film findById(int id) {
      String sqlQuery = "SELECT * FROM films WHERE film_id = ?";

      SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

      if (filmRows.next()) {
          sqlQuery = "SELECT genre_id FROM film_genre WHERE film_id = ?";
          SqlRowSet genreRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

          List<Genre> genres = new ArrayList<>();
          while (genreRows.next()) {
              int genreId = Integer.parseInt(genreRows.getString("genre_id"));
              genres.add(genreDao.findGenreById(genreId));
          }

          sqlQuery = "SELECT user_id FROM likes WHERE film_id = ?";
          SqlRowSet likesRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
          List<Integer> likes = new ArrayList<>();
          while (likesRows.next()) {
              likes.add(Integer.parseInt(likesRows.getString("user_id")));
          }

          Film film = new Film(
                            Integer.parseInt(filmRows.getString("FILM_ID")),
                            filmRows.getString("NAME").trim(),
                            filmRows.getString("DESCRIPTION").trim(),
                            LocalDate.parse(filmRows.getString("RELEASE_DATE").trim(), formatter),
                            Integer.parseInt(filmRows.getString("DURATION")),
                  mpaRatingDao.findMpaById(Integer.parseInt(filmRows.getString("RATING_ID"))),
                  likes,
                  genres
                    );

          log.info("Найден фильм: {} {}", film.getId(), film.getName());
          return film;
      } else {
          log.warn("Фильм с id = {} не найден", id);
          throw new StorageException(String.format("Фильм с id = %d не найден", id));
      }
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        String sqlQuery = "DELETE FROM likes " +
                "WHERE film_id = ? " +
                "AND user_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId, userId);

        log.info("Пользователь {} убрал лайк фильму {}.", userId, filmId);
        return findById(filmId);
    }

    private List<Genre> updateGenres(List<Genre> genres, Integer filmId) {
        List<Genre> genresResult = new ArrayList<>();
        String sqlQuery = "DELETE FROM film_genre " +
                "WHERE film_id = ?;";

        jdbcTemplate.update(sqlQuery, filmId);

        if (genres != null && !genres.isEmpty()) {
            genres = genres.stream()
                    .distinct()
                    .collect(Collectors.toList());
            for (Genre genre : genres) {
                sqlQuery = "INSERT INTO film_genre (film_id, genre_id) " +
                        "VALUES (?, ?);";

                jdbcTemplate.update(sqlQuery, filmId, genre.getId());
                genre = genreDao.findGenreById(genre.getId());
                genresResult.add(genre);
            }
        }
        return genresResult;
    }

    private List<Integer> updateLikes(List<Integer> likes, Integer filmId) {
        String sqlQuery = "DELETE FROM likes " +
                "WHERE film_id = ?;";

        jdbcTemplate.update(sqlQuery, filmId);

        if (likes == null || likes.isEmpty()) {
            return likes;
        }
        likes = likes.stream()
                .distinct().collect(Collectors.toList());
        for (Integer like : likes) {
            sqlQuery = "INSERT INTO likes (user_id, film_id) " +
                    "VALUES (?, ?);";

            jdbcTemplate.update(sqlQuery, filmId, like);
        }
        return likes;
    }

    private void findMatch(Film film) {
        String sqlQuery = "SELECT * FROM films " +
                "WHERE (NAME=?) " +
                "AND (RELEASE_DATE=?) AND (DURATION=?);";
        SqlRowSet filmsRow = jdbcTemplate.queryForRowSet(sqlQuery, film.getName(), film.getReleaseDate(), film.getDuration());
        if (filmsRow.next()) {
            int id = Integer.parseInt(filmsRow.getString("film_id"));
            throw new StorageException(String.format("Фильм уже существует, id = %d", id));
        }
    }
}

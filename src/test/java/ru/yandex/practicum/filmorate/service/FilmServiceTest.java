package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.StorageException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {
    private final FilmService filmService;
    private final UserService userService;

    Film film = Film.builder()
            .id(-1)
            .name("name")
            .description("description")
            .releaseDate(LocalDate.now())
            .duration(120)
            .mpa(MpaRating.builder().id(1).build())
            .build();

    @Test
    public void testCreateFilmMustReturnUserWithNewId() {
        film.setName("testCreateFilmMustReturnUserWithNewId");
        film = filmService.create(film);
        assertFalse(film.getId() == -1, "Фильм не записан в БД, id не был присвоен");
    }

    @Test
    public void testCreateFilmWithWrongParameters() {
        Film filmTest = Film.builder()
                .id(-1)
                .name("testCreateFilmWithWrongParameters")
                .description("description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .mpa(MpaRating.builder().name("G").build())
                .build();

        final ValidationException exceptionMpa = assertThrows(
                ValidationException.class,
                () -> filmService.create(filmTest)
        );

        assertEquals("Получены некорректные данные. Не указан id рейтинга фильма.", exceptionMpa.getMessage());

        List<Genre> genres = new ArrayList<>();
        genres.add(Genre.builder().name("Комедия").build());
        filmTest.setMpa(MpaRating.builder().id(1).build());
        filmTest.setGenres(genres);

        final ValidationException exceptionGenre = assertThrows(
                ValidationException.class,
                () -> filmService.create(filmTest)
        );

        assertEquals("Получены некорректные данные. Не указан id жанра Комедия фильма.", exceptionGenre.getMessage());
    }

    @Test
    public void testCreateFilmSimilar() {
        film.setName("testCreateFilmSimilar");
        int id = filmService.create(film).getId();
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> filmService.create(film)
        );

        assertEquals("Фильм уже существует, id = " + id, exception.getMessage());
    }

    @Test
    public void testFindFilmByWrongId() {
        final StorageException exception = assertThrows(
                StorageException.class,
                () -> filmService.getFilm(-1)
        );

        assertEquals("Фильм с id = -1 не найден", exception.getMessage());
    }

    @Test
    public void testUpdateFilmNormalId() {
        film.setName("testUpdateFilmNormalId");
        film = filmService.create(film);
        List<Genre> genres = new ArrayList<>();
        genres.add(Genre.builder().id(1).build());
        genres.add(Genre.builder().id(3).build());
        film.setMpa(MpaRating.builder().id(2).build());
        film.setGenres(genres);
        filmService.update(film);
        Film expectedFilm = filmService.getFilm(film.getId());

        assertTrue(expectedFilm.getMpa().getId() == 2, "Не выполнено обновления фильма. Не записан рейтинг.");
        assertTrue(expectedFilm.getGenres().size() == 2, "Не выполнено обновления фильма. Не записаны жанры.");
    }

    @Test
    public void testAddLikeFilm() {
        film.setName("testAddLikeFilm");
        film = filmService.create(film);
        User userTest = User.builder()
                .login("login")
                .email("email@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userTest = userService.create(userTest);
        filmService.like(film.getId(), userTest.getId());
        film = filmService.getFilm(film.getId());

        assertTrue(film.getLikes().size() == 1, "Лайк не был поставлен");
    }

    @Test
    public void testDeleteLikeFilm() {
        film.setName("testDeleteLikeFilm");
        film = filmService.create(film);
        User userTest = User.builder()
                .login("testDeleteLikeFilm")
                .email("testDeleteLikeFilm@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userTest = userService.create(userTest);
        filmService.like(film.getId(), userTest.getId());
        filmService.removeLike(film.getId(), userTest.getId());
        film = filmService.getFilm(film.getId());

        assertTrue(film.getLikes().isEmpty(), "Лайк не был удален");
    }

    @Test
    void testGetPopularFilms() {
        film = filmService.create(film);
        User userTest = User.builder()
                .login("testLogin")
                .email("testCreate@ya.ru")
                .birthday(LocalDate.now())
                .build();
        userTest = userService.create(userTest);
        filmService.like(film.getId(), userTest.getId());
        film.setId(-1);
        film.setName("Name2");
        filmService.create(film);

        List<Film> films = filmService.getTopCountPopularFilms(2);
        assertTrue(films.size() == 2, "Неправильно выдает популярные фильмы");
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {
    private final FilmService filmStorage;

    MpaRating mpa = MpaRating.builder()
            .id(1)
            .name("G")
            .description("У фильма нет возрастных ограничений.")
            .build();

    @BeforeEach
    void fillDB() {
        Film film = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1967, 3, 25), 100, mpa);
        filmStorage.create(film);
    }

    @Test
    void testCreateFilm() {
        Film film = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1967, 3, 25), 100, mpa);
        filmStorage.create(film);
        assertThat(film).hasFieldOrPropertyWithValue("id", 0);
        assertThat(film).hasFieldOrPropertyWithValue("name", "nisi eiusmod");
    }

    @Test
    void testUpdateFilm() {
        Genre genre = Genre.builder()
                .id(2)
                .name("Драма")
                .build();
        Set<Genre> genres = new HashSet<>();
        genres.add(genre);
        Film update = new Film("update", "newDescription", LocalDate.of(1977, 3, 25), 100, mpa, genres);
        update.setId(1);
        Film expectFilm = filmStorage.update(update);
        assertThat(expectFilm).hasFieldOrPropertyWithValue("id", 1);
        assertThat(expectFilm).hasFieldOrPropertyWithValue("name", "update");
        assertThat(expectFilm).hasFieldOrPropertyWithValue("description", "newDescription");
        assertEquals(1, expectFilm.getGenres().size());
    }

    @Test
    void testGetAllFilms() {
        Film film = new Film("New film", "New film about friends", LocalDate.of(1999, 4, 30), 120, mpa);
        filmStorage.create(film);
        List<Film> allFilms = filmStorage.getFilms();
        assertEquals(2, allFilms.size());
        assertThat(allFilms.get(0)).hasFieldOrPropertyWithValue("id", 1);
        assertThat(allFilms.get(1)).hasFieldOrPropertyWithValue("id", 2);
    }
}

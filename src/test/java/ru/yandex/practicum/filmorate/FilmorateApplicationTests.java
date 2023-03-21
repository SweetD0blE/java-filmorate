package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UpdateException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmorateApplicationTests {
	private FilmController filmController;
	private Film film;
	private UserController userController;
	private User user;

	@BeforeEach
	public void beforeEach() {
		filmController = new FilmController();
		film = new Film();
		film.setName("Name");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100L);

		userController = new UserController();
		user = new User();
		user.setName("Name");
		user.setLogin("Login");
		user.setBirthday(LocalDate.now());
		user.setEmail("user@mail.ru");
	}

	@Test
	public void shouldThrowValidationExceptionInFilmController() {
		film.setReleaseDate(LocalDate.of(1800, 6, 5));

		final ValidationException e = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Ошибка валидации: Дата релиза фильма не раньше 28 декабря 1895 года.", e.getDetailMessage());
	}

	@Test
	public void shouldThrowUpdateExceptionInFilmController() {
		film.setId(1);

		final UpdateException e = assertThrows(UpdateException.class, () -> filmController.update(film));
		assertEquals("Ошибка обновления: Фильма с id=" + film.getId() + " не существует", e.getDetailMessage());
	}

	@Test
	public void shouldThrowUpdateExceptionInUserController() {
		user.setId(1);

		final UpdateException e = assertThrows(UpdateException.class, () -> userController.update(user));
		assertEquals("Ошибка обновления: Пользователя с id=" + user.getId() + " не существует", e.getDetailMessage());
	}

	@Test
	public void validateEmailNullTest() {
		user.setEmail(null);

		final ValidationException e = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Ошибка валидации: Электронная почта не может быть пустой и должна содержать символ @", e.getDetailMessage());
	}

	@Test
	public void validateEmailIsBlankTest() {
		user.setEmail("");

		final ValidationException e = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Ошибка валидации: Электронная почта не может быть пустой и должна содержать символ @", e.getDetailMessage());
	}

	@Test
	public void validateLoginNullTest() {
		user.setLogin(null);

		final ValidationException e = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Ошибка валидации: Логин не может быть пустым и содержать пробелы", e.getDetailMessage());
	}

	@Test
	public void validateLoginIsBlankTest() {
		user.setLogin("");

		final ValidationException e = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Ошибка валидации: Логин не может быть пустым и содержать пробелы", e.getDetailMessage());
	}

	@Test
	public void validateLoginTest() {
		user.setLogin("user 1234");

		final ValidationException e = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Ошибка валидации: Логин не может быть пустым и содержать пробелы", e.getDetailMessage());
	}
}

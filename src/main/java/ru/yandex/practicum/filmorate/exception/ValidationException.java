package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public String getDetailMessage() {
        return "Ошибка валидации: " + getMessage();
    }
}

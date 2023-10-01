package ru.practicum.ewm.service.error;

public class BadRequestError extends RuntimeException {
    public BadRequestError(String message) {
        super(message);
    }
}

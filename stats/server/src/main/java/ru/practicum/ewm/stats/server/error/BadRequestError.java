package ru.practicum.ewm.stats.server.error;

public class BadRequestError extends RuntimeException {
    public BadRequestError(String message) {
        super(message);
    }
}

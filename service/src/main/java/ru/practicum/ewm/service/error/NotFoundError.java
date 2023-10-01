package ru.practicum.ewm.service.error;

public class NotFoundError extends RuntimeException {
    public NotFoundError(String message) {
        super(message);
    }
}

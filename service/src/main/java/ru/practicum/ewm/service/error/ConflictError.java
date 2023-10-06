package ru.practicum.ewm.service.error;

public class ConflictError extends RuntimeException {
    public ConflictError(String message) {
        super(message);
    }
}

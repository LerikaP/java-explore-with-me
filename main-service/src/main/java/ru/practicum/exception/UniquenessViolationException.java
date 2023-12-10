package ru.practicum.exception;

public class UniquenessViolationException extends RuntimeException {

    public UniquenessViolationException(String message) {
        super(message);
    }
}
package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUniquenessViolationException(final UniquenessViolationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIntegrityConstraintViolationException(final ValidationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final BadRequestException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return new ErrorResponse(e.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }
}

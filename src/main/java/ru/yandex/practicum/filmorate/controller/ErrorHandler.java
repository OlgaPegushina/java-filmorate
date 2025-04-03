package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@Slf4j
@RestControllerAdvice()
public class ErrorHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse validationExceptionHandle(Exception e) {
        log.error("Ошибка: {}({}).", e.getClass().getSimpleName(), e.getMessage());
        return new ExceptionResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class, EmptyResultDataAccessException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse storageNotFoundExceptionHandle(Exception e) {
        log.warn("Ошибка: {}({}).", e.getClass().getSimpleName(), e.getMessage());
        return new ExceptionResponse("Запрашиваемый ресурс не найден", e.getMessage());
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse internalServerExceptionHandle(final InternalServerException i) {
        log.error("Ошибка: {}({}).", i.getClass().getSimpleName(), i.getMessage());
        return new ExceptionResponse("Ошибка сервера", i.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse throwableHandle(final Throwable t) {
        log.error("Ошибка: {}({}).", t.getClass().getSimpleName(), t.getMessage());
        return new ExceptionResponse("Ошибка сервера", t.getMessage());
    }

    @Getter
    @AllArgsConstructor
    static class ExceptionResponse {
        String error;
        String description;
    }
}
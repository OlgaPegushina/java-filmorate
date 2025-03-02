package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@Slf4j
@RestControllerAdvice()
public class ErrorHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse validationExceptionHandle(Exception e) {
        log.info("Ошибка: {}({}).", e.getClass().getSimpleName(), e.getMessage());
        return new ExceptionResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse storageNotFoundExceptionHandle(final NotFoundException e) {
        log.info("Ошибка: {}({}).", e.getClass().getSimpleName(), e.getMessage());
        return new ExceptionResponse("Запрашиваемый ресурс не найден", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse throwableHandle(final Throwable t) {
        log.info("Ошибка: {}({}).", t.getClass().getSimpleName(), t.getMessage());
        return new ExceptionResponse("Ошибка сервера", t.getMessage());
    }

    @Getter
    @AllArgsConstructor
    static class ExceptionResponse {
        String error;
        String description;
    }
}
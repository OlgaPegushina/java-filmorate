package ru.yandex.practicum.filmorate.model.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFilmSortByConverter implements Converter<String, FilmSortBy> {
    @Override
    public FilmSortBy convert(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return null;
        }
        try {
            return FilmSortBy.valueOf(sortBy.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимое значение для сортировки: " + sortBy);
        }
    }
}
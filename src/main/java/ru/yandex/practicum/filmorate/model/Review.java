package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode(of = {"reviewId"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long reviewId; // название по JSON

    @NotNull(message = "ID фильма обязателен")
    Long filmId;

    @NotNull(message = "ID пользователя обязателен")
    Long userId;

    @NotBlank(message = "Отзыв не может быть пустым")
    String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    Boolean isPositive;

    int useful;
}

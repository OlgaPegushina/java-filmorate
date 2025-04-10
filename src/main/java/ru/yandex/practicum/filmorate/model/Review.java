package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = {"reviewId"})
public class Review {
    private Long reviewId; // название по JSON

    @NotNull(message = "ID фильма обязателен")
    Long filmId;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @NotBlank(message = "Отзыв не может быть пустым")
    String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;

    private int useful;
}

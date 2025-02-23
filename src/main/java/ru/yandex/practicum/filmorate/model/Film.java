package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "id" })
@Builder
public class Film {
    private long id;

    @NotBlank
    private String name;

    private String description;
    private LocalDate releaseDate;
    private long duration;
}

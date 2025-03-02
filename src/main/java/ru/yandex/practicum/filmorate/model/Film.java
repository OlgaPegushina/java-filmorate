package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = { "id" })
@Builder
public class Film {
    private long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно больше 200 символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private long duration;

    @JsonIgnore
    private final Set<Long> likes = new HashSet<>();
}

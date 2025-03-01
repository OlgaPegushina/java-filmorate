package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = { "id" })
@Builder
public class User {
    private long id;

    @NotNull
    @Email(message = "Некорректная почта")
    private String email;

    @NotBlank(message = "Логин не может быть пустым или с пробелами")
    private String login;

    @NotNull
    private String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private final Set<Long> friends = new HashSet<>();
}

package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "id" })
@Builder
public class User {
    private long id;

    @Email
    private String email;

    @NotBlank (message = "Логин не может быть пустым или с пробелами")
    private String login;

    private String name;
    private LocalDate birthday;
}

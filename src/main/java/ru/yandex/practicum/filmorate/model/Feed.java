package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Feed {
    @NotNull
    Long timestamp;

    @NotNull(message = "Значение не может быть пустым")
    Long userId;

    @NotNull
    EventType eventType;

    @NotNull
    Long eventId;

    @NotNull
    @JsonProperty("operation")
    EventOperation eventOperation;

    @NotNull
    Long entityId;
}
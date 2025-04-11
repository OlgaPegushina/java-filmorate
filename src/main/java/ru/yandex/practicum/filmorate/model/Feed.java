package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@Getter
@Builder
public class Feed {
    private Long timestamp;

    @NotBlank(message = "Значение не может быть пустым")
    private Long userId;

    private EventType eventType;

    private Long eventId;

    @JsonProperty("operation")
    private EventOperation eventOperation;

    private Long entityId;
}
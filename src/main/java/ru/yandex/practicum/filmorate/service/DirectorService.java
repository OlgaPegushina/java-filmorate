package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorService {
    DirectorStorage directorStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == 0) {
            throw new ValidationException("ID не может быть равен 0");
        }
        return directorStorage.update(director);
    }

    public Director getById(Long directorId) {
        return directorStorage.getById(directorId)
                .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id %d не найден.", directorId)));
    }

    public Collection<Director> findAllValues() {
        return directorStorage.getAllValues();
    }

    public void delete(Long id) {
        directorStorage.deleteById(id);
    }
}

package ru.yandex.practicum.filmorate.dal.storage.director;


import ru.yandex.practicum.filmorate.dal.storage.Storage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage extends Storage<Director> {
    List<Director> getByFilmId(Long filmId);
}


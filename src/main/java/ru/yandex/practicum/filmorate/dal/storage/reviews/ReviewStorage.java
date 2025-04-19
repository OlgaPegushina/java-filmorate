package ru.yandex.practicum.filmorate.dal.storage.reviews;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Optional<Review> update(Review review);

    void deleteById(Long reviewId);

    Optional<Review> findById(Long reviewId);

    List<Review> findAllByFilmId(Long filmId, int count);

    List<Review> findAll(int count);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeReaction(Long reviewId, Long userId);
}

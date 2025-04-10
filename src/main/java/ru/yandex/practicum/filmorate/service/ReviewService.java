package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.reviews.ReviewDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbRepository reviewRepository;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review createReview(Review review) {
        validate(review);
        return reviewRepository.create(review);
    }

    public Review updateReview(Review review) {
        validate(review);
        return reviewRepository.update(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: " + id));
    }

    public List<Review> getReviews(Long filmId, int count) {
        return (filmId != null)
                ? reviewRepository.findAllByFilmId(filmId, count)
                : reviewRepository.findAll(count);
    }

    public void addLike(Long reviewId, Long userId) {
        reviewRepository.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewRepository.addDislike(reviewId, userId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        reviewRepository.removeReaction(reviewId, userId);
    }

    private void validate(Review review) {
        userStorage.getById(review.getUserId());
        filmStorage.getById(review.getFilmId());
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.dal.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.reviews.ReviewDbRepository;
import ru.yandex.practicum.filmorate.dal.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {
    ReviewDbRepository reviewRepository;
    UserStorage userStorage;
    FilmStorage filmStorage;
    FeedStorage feedStorage;

    public Review createReview(Review review) {
        validateReviewAndUserExist(review);
        Review createReview = reviewRepository.create(review);
        feedStorage.addEvent(createReview.getUserId(), createReview.getReviewId(), EventOperation.ADD, EventType.REVIEW);
        return createReview;
    }

    public Review updateReview(Review review) {
        validateReviewAndUserExist(review);
        Review updateReview = reviewRepository.update(review)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id %d не найден.", review.getReviewId())));
        feedStorage.addEvent(updateReview.getUserId(), updateReview.getReviewId(), EventOperation.UPDATE, EventType.REVIEW);
        return updateReview;
    }

    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id %d не найден: ", id)));
        Long userId = review.getUserId();
        try {
            feedStorage.addEvent(userId, id, EventOperation.REMOVE, EventType.REVIEW);
            reviewRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Ошибка при удалении отзыва", e);
        }
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id %d не найден: ", id)));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (filmId != null) {
            filmStorage.validateFilm(filmId);
            return reviewRepository.findAllByFilmId(filmId, count);
        } else {
            return reviewRepository.findAll(count);
        }
    }

    public void addLike(Long reviewId, Long userId) {
        validateReactionEntities(reviewId, userId);
        reviewRepository.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReactionEntities(reviewId, userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        validateReactionEntities(reviewId, userId);
        reviewRepository.removeReaction(reviewId, userId);
    }

    private void validateReviewAndUserExist(Review review) {
        userStorage.validateUser(review.getUserId());
        filmStorage.validateFilm(review.getFilmId());
    }

    private void validateReactionEntities(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId);
        userStorage.validateUser(userId);
    }
}

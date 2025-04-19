package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewsController {
    ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @GetMapping("{id}")
    public Review findById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping
    public List<Review> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
    }
}

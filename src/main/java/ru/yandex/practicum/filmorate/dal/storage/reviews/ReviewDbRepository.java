package ru.yandex.practicum.filmorate.dal.storage.reviews;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbRepository extends BaseRepository<Review> implements ReviewStorage {
    private final RowMapper<Review> mapper = new ReviewRowMapper();

    public ReviewDbRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO review (film_id, user_id, content, is_positive, useful) VALUES (?, ?, ?, ?, ?)";
        long id = super.create(sql,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getIsPositive(),
                review.getUseful()
        );
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE review SET content = ?, is_positive = ? WHERE review_id = ?";
        super.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        review.setReviewId(review.getReviewId());
        review.setFilmId(review.getReviewId());
        review.setUserId(review.getReviewId());
        return review;
    }

    @Override
    public void deleteById(Long reviewId) {
        jdbc.update("DELETE FROM review WHERE review_id = ?", reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return jdbc.query(
                        "SELECT * FROM review WHERE review_id = ?", mapper, reviewId)
                .stream()
                .findFirst();
    }

    @Override
    public List<Review> findAllByFilmId(Long filmId, int count) {
        String sql = filmId != null
                ? "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM review ORDER BY useful DESC LIMIT ?";
        return filmId != null
                ? jdbc.query(sql, mapper, filmId, count)
                : jdbc.query(sql, mapper, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String sql = """
                SELECT r.*, COALESCE(SUM(ru.is_useful), 0) AS useful
                FROM review r
                LEFT JOIN review_users ru ON r.review_id = ru.review_id
                GROUP BY r.review_id
                ORDER BY useful DESC
                LIMIT ?
                """;
        return jdbc.query(sql, mapper, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        removeReaction(reviewId, userId); // удаляем, если был дизлайк
        jdbc.update("INSERT INTO review_users (review_id, user_id, is_useful) VALUES (?, ?, TRUE)", reviewId, userId);
        incrementUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        removeReaction(reviewId, userId); // удаляем, если был лайк
        String sql = "INSERT INTO review_users (review_id, user_id, is_useful) VALUES (?, ?, FALSE)";
        jdbc.update(sql, reviewId, userId);
        decrementUseful(reviewId);
    }

    @Override
    public void removeReaction(Long reviewId, Long userId) {
        Boolean wasUseful = getRatingType(reviewId, userId);
        jdbc.update("DELETE FROM review_users WHERE review_id = ? AND user_id = ?", reviewId, userId);
        if (wasUseful != null) {
            if (wasUseful) decrementUseful(reviewId);
            else incrementUseful(reviewId);
        }
    }

    private Boolean getRatingType(Long reviewId, Long userId) {
        String sql = "SELECT is_useful FROM review_users WHERE review_id = ? AND user_id = ?";
        List<Boolean> result = jdbc.query(sql,
                (rs, rowNum) -> rs.getBoolean("is_useful"), reviewId, userId);
        return result.isEmpty() ? null : result.getFirst();
    }

    private void incrementUseful(Long reviewId) {
        jdbc.update("UPDATE review SET useful = useful + 1 WHERE review_id = ?", reviewId);
    }

    private void decrementUseful(Long reviewId) {
        jdbc.update("UPDATE review SET useful = useful - 1 WHERE review_id = ?", reviewId);
    }
}

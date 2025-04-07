package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Построение девиаций
 */
@Service
public class RecommendationService {
    public Map<Long, Map<Long, Deviation>> buildDeviations(Map<Long, Map<Long, Double>> userRatings) {
        Map<Long, Map<Long, Deviation>> deviations = new HashMap<>();

        for (var ratings : userRatings.values()) {
            for (var iEntry : ratings.entrySet()) {
                Long i = iEntry.getKey();
                double rI = iEntry.getValue();

                for (var jEntry : ratings.entrySet()) {
                    Long j = jEntry.getKey();
                    double rJ = jEntry.getValue();
                    if (i.equals(j)) continue;

                    deviations
                            .computeIfAbsent(i, k -> new HashMap<>())
                            .computeIfAbsent(j, k -> new Deviation())
                            .add(rI - rJ);
                }
            }
        }

        return deviations;
    }

    /**
     * Предсказание оценки
     * */
    public double predictRating(Long userId, Long targetFilmId,
                                Map<Long, Map<Long, Double>> userRatings,
                                Map<Long, Map<Long, Deviation>> deviations) {

        Map<Long, Double> userRated = userRatings.get(userId);
        if (userRated == null || userRated.isEmpty()) return 0.0;

        double numerator = 0.0;
        int denominator = 0;

        for (var entry : userRated.entrySet()) {
            Long otherFilm = entry.getKey();
            double rating = entry.getValue();

            Deviation dev = Optional.ofNullable(deviations.get(targetFilmId))
                    .map(m -> m.get(otherFilm))
                    .orElse(null);

            if (dev != null && dev.count > 0) {
                numerator += (rating + dev.average()) * dev.count;
                denominator += dev.count;
            }
        }

        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    public static class Deviation {
        double sum = 0.0;
        int count = 0;

        public void add(double value) {
            this.sum += value;
            this.count++;
        }

        public double average() {
            return count == 0 ? 0.0 : sum / count;
        }
    }
}

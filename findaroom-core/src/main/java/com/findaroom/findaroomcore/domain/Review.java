package com.findaroom.findaroomcore.domain;

import com.findaroom.findaroomcore.controller.event.ReviewAccommodation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@Document(collection = "reviews")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {

    @Id
    String reviewId;
    @Indexed
    String accommodationId;
    @Indexed
    String userId;
    @Indexed(unique = true)
    String bookingId;
    double rating;
    @TextIndexed
    String message;
    Instant createTime;

    public static Review of(String accommodationId, String userId, String bookingId, double rating, String message) {
        return new Review(null, accommodationId, userId, bookingId, rating, message, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public static Review from(String accommodationId, String userId, String bookingId, ReviewAccommodation review) {
        return Review.of(
                accommodationId,
                userId,
                bookingId,
                review.getRating(),
                review.getMessage()
        );
    }
}

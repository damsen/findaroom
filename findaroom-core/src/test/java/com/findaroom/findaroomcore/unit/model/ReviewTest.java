package com.findaroom.findaroomcore.unit.model;

import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.model.Review;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.TestPojos.reviewAccommodation;
import static org.assertj.core.api.Assertions.assertThat;

public class ReviewTest {

    @Test
    public void fromReview_shouldReturnReviewWithMatchingProperties() {

        ReviewAccommodation review = reviewAccommodation();
        Review rev = Review.from("123", "444", "111", review);

        assertThat(rev.getAccommodationId()).isEqualTo("123");
        assertThat(rev.getUserId()).isEqualTo("444");
        assertThat(rev.getBookingId()).isEqualTo("111");
        assertThat(rev.getMessage()).isEqualTo(review.getMessage());
        assertThat(rev.getRating()).isEqualTo(review.getRating());
    }
}

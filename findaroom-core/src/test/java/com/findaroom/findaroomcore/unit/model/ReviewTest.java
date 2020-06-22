package com.findaroom.findaroomcore.unit.model;

import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.model.Review;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.PojoUtils.reviewAccommodation;
import static org.assertj.core.api.Assertions.assertThat;

public class ReviewTest {

    @Test
    public void fromReview_shouldReturnReviewWithMatchingProperties() {

        ReviewAccommodation review = reviewAccommodation();
        Review rev = Review.from(review);

        assertThat(rev.getAccommodationId()).isEqualTo(review.getAccommodationId());
        assertThat(rev.getUserId()).isEqualTo(review.getUserId());
        assertThat(rev.getBookingId()).isEqualTo(review.getBookingId());
        assertThat(rev.getMessage()).isEqualTo(review.getMessage());
        assertThat(rev.getRating()).isEqualTo(review.getRating());
    }
}

package com.findaroom.findaroomcore.unit.repo;

import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;

import static com.findaroom.findaroomcore.utils.PojoUtils.review;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repo;

    @BeforeEach
    public void setup() {
        repo.deleteAll().block();
    }

    @Test
    public void findAllByFilter() {

        Flux<Review> reviews = repo
                .saveAll(Flux.just(review(), review()))
                .thenMany(repo.findAllByFilter(new ReviewSearchFilter()));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getReviewId()).isNotNull())
                .assertNext(r -> assertThat(r.getReviewId()).isNotNull())
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withAccommodationIdFilter_shouldReturnFilteredResults() {

        Review rev1 = review();
        rev1.setAccommodationId("123");
        Review rev2 = review();
        rev2.setAccommodationId("456");

        var filter = new ReviewSearchFilter();
        filter.setAccommodationId("123");

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getAccommodationId()).isEqualTo("123"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withUserIdFilter_shouldReturnFilteredResults() {

        Review rev1 = review();
        rev1.setUserId("123");
        Review rev2 = review();
        rev2.setUserId("456");

        var filter = new ReviewSearchFilter();
        filter.setUserId("123");

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getUserId()).isEqualTo("123"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withBookingIdFilter_shouldReturnFilteredResults() {

        Review rev1 = review();
        rev1.setBookingId("123");
        Review rev2 = review();
        rev2.setBookingId("456");

        var filter = new ReviewSearchFilter();
        filter.setBookingId("123");

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getBookingId()).isEqualTo("123"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withRatingFilter_shouldReturnFilteredResults() {

        Review rev1 = review();
        rev1.setRating(3.0);
        Review rev2 = review();
        rev2.setRating(5.0);

        var filter = new ReviewSearchFilter();
        filter.setRating(4.0);

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getRating()).isGreaterThanOrEqualTo(4.0))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withTextFilter_shouldReturnFilteredResults() {

        Review rev1 = review();
        rev1.setMessage("Great place");
        Review rev2 = review();
        rev2.setMessage("This place is incredible");
        Review rev3 = review();
        rev3.setMessage("Had a blast");

        var filter = new ReviewSearchFilter();
        filter.setQ("PLACE incredible");

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2, rev3))
                .thenMany(repo.findAllByFilter(filter));

        Predicate<Review> place = r -> r.getMessage().toLowerCase().contains("PLACE".toLowerCase());
        Predicate<Review> incredible = r -> r.getMessage().toLowerCase().contains("incredible".toLowerCase());
        StepVerifier
                .create(reviews)
                .expectNextMatches(place.or(incredible))
                .expectNextMatches(place.or(incredible))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withPaging_shouldReturnPagedResults() {

        Review rev1 = review();
        Review rev2 = review();
        Review rev3 = review();

        var filter = new ReviewSearchFilter();
        filter.setPage(0);
        filter.setSize(2);

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2, rev3))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withSorting_shouldReturnSortedResults() {

        Review rev1 = review();
        rev1.setRating(1.0);
        Review rev2 = review();
        rev2.setRating(5.0);
        Review rev3 = review();
        rev3.setRating(3.0);
        Review rev4 = review();
        rev4.setRating(4.5);

        var filter = new ReviewSearchFilter();
        filter.setSortBy(List.of("rating"));
        filter.setDirection("DESC");

        Flux<Review> reviews = repo
                .saveAll(Flux.just(rev1, rev2, rev3, rev4))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(reviews)
                .assertNext(r -> assertThat(r.getRating()).isEqualTo(5.0))
                .assertNext(r -> assertThat(r.getRating()).isEqualTo(4.5))
                .assertNext(r -> assertThat(r.getRating()).isEqualTo(3.0))
                .assertNext(r -> assertThat(r.getRating()).isEqualTo(1.0))
                .verifyComplete();
    }
}

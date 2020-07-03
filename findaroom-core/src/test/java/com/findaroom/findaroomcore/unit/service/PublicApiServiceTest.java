package com.findaroom.findaroomcore.unit.service;

import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.service.PublicApiService;
import com.findaroom.findaroomcore.utils.TestPojos;
import com.findaroom.findaroomcore.utils.TestPredicates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static com.findaroom.findaroomcore.utils.MessageUtils.ACCOMMODATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicApiServiceTest {

    @MockBean
    private AccommodationRepository accommodationRepo;

    @MockBean
    private BookingRepository bookingRepo;
    
    @MockBean
    private ReviewRepository reviewRepo;
    
    private PublicApiService publicApi;

    @BeforeAll
    public void setup() {
        publicApi = new PublicApiService(accommodationRepo, bookingRepo, reviewRepo);
    }

    @Test
    public void findAccommodationsByFilter_withNoDateFilters() {

        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.empty());
        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.accommodation(), TestPojos.accommodation()));

        var filter = new AccommodationSearchFilter();
        Flux<Accommodation> accommodations = publicApi.findAccommodationsByFilter(filter);

        StepVerifier
                .create(accommodations)
                .expectNextCount(2)
                .verifyComplete();

        assertThat(filter.getExclude().block()).isEmpty();
    }

    @Test
    public void findAccommodationsByFilter_withDateFilters() {

        Booking book1 = TestPojos.booking();
        book1.setAccommodationId("123");
        Booking book2 = TestPojos.booking();
        book2.setAccommodationId("456");
        Booking book3 = TestPojos.booking();
        book3.setAccommodationId("456");
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(book1, book2, book3));
        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.accommodation(), TestPojos.accommodation()));

        var filter = new AccommodationSearchFilter();
        filter.setCheckin(LocalDate.now());
        filter.setCheckout(LocalDate.now().plusDays(5));
        Flux<Accommodation> accommodations = publicApi.findAccommodationsByFilter(filter);

        StepVerifier
                .create(accommodations)
                .expectNextCount(2)
                .verifyComplete();

        assertThat(filter.getExclude().block()).containsExactlyInAnyOrder("123", "456");
    }

    @Test
    public void findAccommodationById() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(TestPojos.accommodation()));

        Mono<Accommodation> accommodation = publicApi.findAccommodationById("123");

        StepVerifier
                .create(accommodation)
                .assertNext(a -> assertThat(a).isNotNull())
                .verifyComplete();
    }

    @Test
    public void findAccommodationById_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());

        Mono<Accommodation> accommodation = publicApi.findAccommodationById("123");

        StepVerifier
                .create(accommodation)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void findAccommodationReviewsByFilter() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.review(), TestPojos.review()));

        Flux<Review> reviews = publicApi.findAccommodationReviewsByFilter("123", new ReviewSearchFilter());

        StepVerifier
                .create(reviews)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findAccommodationReviewsByFilter_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());

        Flux<Review> reviews = publicApi.findAccommodationReviewsByFilter("123", new ReviewSearchFilter());

        StepVerifier
                .create(reviews)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void findAccommodationReviewsByFilter_whenReviewsNotFound_shouldReturnEmpty() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.empty());

        Flux<Review> reviews = publicApi.findAccommodationReviewsByFilter("123", new ReviewSearchFilter());

        StepVerifier
                .create(reviews)
                .verifyComplete();
    }
}

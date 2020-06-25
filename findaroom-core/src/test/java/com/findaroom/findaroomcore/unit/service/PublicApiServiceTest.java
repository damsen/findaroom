package com.findaroom.findaroomcore.unit.service;

import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepo;
import com.findaroom.findaroomcore.repo.BookingRepo;
import com.findaroom.findaroomcore.repo.ReviewRepo;
import com.findaroom.findaroomcore.service.PublicApiService;
import com.findaroom.findaroomcore.utils.PojoUtils;
import com.findaroom.findaroomcore.utils.PredicateUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PublicApiServiceTest {

    @MockBean
    private AccommodationRepo accommodationRepo;

    @MockBean
    private BookingRepo bookingRepo;
    
    @MockBean
    private ReviewRepo reviewRepo;
    
    private PublicApiService publicApi;

    @BeforeAll
    public void setup() {
        publicApi = new PublicApiService(accommodationRepo, bookingRepo, reviewRepo);
    }

    @Test
    public void findAccommodationsByFilter_withNoDateFilters() {

        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.empty());
        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.accommodation(), PojoUtils.accommodation()));

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

        Booking book1 = PojoUtils.booking();
        book1.setAccommodationId("123");
        Booking book2 = PojoUtils.booking();
        book2.setAccommodationId("456");
        Booking book3 = PojoUtils.booking();
        book3.setAccommodationId("456");
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(book1, book2, book3));
        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.accommodation(), PojoUtils.accommodation()));

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

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));

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
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void findAccommodationReviewsByFilter() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.review(), PojoUtils.review()));

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
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void findAccommodationReviewsByFilter_whenReviewsNotFound_shouldReturnEmpty() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.empty());

        Flux<Review> reviews = publicApi.findAccommodationReviewsByFilter("123", new ReviewSearchFilter());

        StepVerifier
                .create(reviews)
                .verifyComplete();
    }
}

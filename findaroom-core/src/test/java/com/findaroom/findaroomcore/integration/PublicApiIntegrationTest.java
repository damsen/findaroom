package com.findaroom.findaroomcore.integration;

import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class PublicApiIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccommodationRepository accommodationRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    public void setup() {
        accommodationRepo.deleteAll().block();
        bookingRepo.deleteAll().block();
        reviewRepo.deleteAll().block();
    }

    @Test
    public void getAccommodations_withNoDateFilters() {

        Accommodation acc1 = PojoUtils.accommodation();
        acc1.setAccommodationId("123");
        Accommodation acc2 = PojoUtils.accommodation();
        acc2.setAccommodationId("456");
        accommodationRepo.saveAll(Flux.just(acc1, acc2)).blockLast();

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].accommodationId", "123").exists()
                .jsonPath("@.[1].accommodationId", "456").exists();
    }

    @Test
    public void getAccommodations_withDateFilters_shouldReturnFilteredResults() {

        Accommodation acc1 = PojoUtils.accommodation();
        acc1.setAccommodationId("123");
        Accommodation acc2 = PojoUtils.accommodation();
        acc2.setAccommodationId("456");
        Accommodation acc3 = PojoUtils.accommodation();
        acc2.setAccommodationId("789");
        accommodationRepo.saveAll(Flux.just(acc1, acc2, acc3)).blockLast();

        Booking book1 = PojoUtils.booking();
        book1.setAccommodationId("123");
        book1.setCheckin(LocalDate.now().plusDays(5));
        book1.setCheckout(LocalDate.now().plusDays(10));
        Booking book2 = PojoUtils.booking();
        book2.setStatus(CANCELLED);
        book2.setAccommodationId("456");
        book2.setCheckin(LocalDate.now().plusDays(4));
        book2.setCheckout(LocalDate.now().plusDays(9));
        bookingRepo.saveAll(Flux.just(book1, book2)).blockLast();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/public/accommodations")
                        .queryParam("checkin", LocalDate.now().plusDays(3))
                        .queryParam("checkout", LocalDate.now().plusDays(8))
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].accommodationId", "456").exists()
                .jsonPath("@.[1].accommodationId", "789").exists();
    }

    @Test
    public void getAccommodationById() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations/{accommodationId}", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodationId", "123").exists();
    }

    @Test
    public void getAccommodationReviews() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Review rev1 = PojoUtils.review();
        rev1.setAccommodationId("123");
        Review rev2 = PojoUtils.review();
        rev2.setAccommodationId("123");
        reviewRepo.saveAll(Flux.just(rev1, rev2)).blockLast();

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations/{accommodationId}/reviews", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Review.class).hasSize(2);
    }
}

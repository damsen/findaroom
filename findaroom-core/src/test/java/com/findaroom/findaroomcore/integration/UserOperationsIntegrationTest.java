package com.findaroom.findaroomcore.integration;

import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.utils.JwtUtils;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.*;
import static com.findaroom.findaroomcore.utils.JwtUtils.addJwt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UserOperationsIntegrationTest {

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
    public void getUserBookings() {

        Booking book1 = PojoUtils.booking();
        book1.setUserId("andrea_damiani@protonmail.com");
        Booking book2 = PojoUtils.booking();
        book2.setUserId("someone_else@example.com");
        bookingRepo.saveAll(Flux.just(book1, book2)).blockLast();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .get()
                .uri("/api/v1/user-ops/my-bookings")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Booking.class).hasSize(1);
    }

    @Test
    public void getUserReviews() {

        Review rev1 = PojoUtils.review();
        rev1.setUserId("andrea_damiani@protonmail.com");
        Review rev2 = PojoUtils.review();
        rev2.setUserId("someone_else@example.com");
        reviewRepo.saveAll(Flux.just(rev1, rev2)).blockLast();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .get()
                .uri("/api/v1/user-ops/my-reviews")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Review.class).hasSize(1);
    }

    @Test
    public void getUserBookingById() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setUserId("andrea_damiani@protonmail.com");
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .get()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}", "111")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.bookingId", "111").exists()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists();
    }

    @Test
    public void saveAccommodation() {

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.createAccommodation())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.host.hostId", "andrea_damiani@protonmail.com").exists()
                .jsonPath("@.host.superHost", "false").exists();
    }

    @Test
    public void bookAccommodation() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.bookAccommodation())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodationId", "123").exists()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists()
                .jsonPath("@.status", "PENDING").exists();
    }

    @Test
    public void bookAccommodation_whenUserIsTheHost_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        accommodationRepo.save(acc).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.bookAccommodation())
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void bookAccommodation_whenMaxGuestsExceeded_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        acc.setMaxGuests(3);
        accommodationRepo.save(acc).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookAccommodation bookAcc = PojoUtils.bookAccommodation();
        bookAcc.setGuests(4);
        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(bookAcc)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void bookAccommodation_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setCheckin(LocalDate.now().plusDays(6));
        book.setCheckout(LocalDate.now().plusDays(12));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookAccommodation bookAcc = PojoUtils.bookAccommodation();
        bookAcc.setBookingDates(new BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)));
        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(bookAcc)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void bookAccommodation_whenAccommodationAlreadyBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setAccommodationId("123");
        book.setCheckin(LocalDate.now().plusDays(6));
        book.setCheckout(LocalDate.now().plusDays(12));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookAccommodation bookAcc = PojoUtils.bookAccommodation();
        bookAcc.setBookingDates(new BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)));
        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(bookAcc)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void reviewAccommodation() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setStatus(DONE);
        book.setCheckout(LocalDate.now().minusDays(2));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        ReviewAccommodation review = PojoUtils.reviewAccommodation();
        review.setMessage("message");
        review.setRating(5.0);
        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/user-ops/accommodations/{accommodationId}/reviews")
                        .queryParam("bookingId", "111")
                        .build("123"))
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodationId", "123").exists()
                .jsonPath("@.bookingId", "111").exists()
                .jsonPath("@.message", "message").exists()
                .jsonPath("@.rating", "5.0").exists()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists();
    }

    @Test
    public void reviewAccommodation_whenBookingNotComplete_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setBookingId("111");
        book.setAccommodationId("123");
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/user-ops/accommodations/{accommodationId}/reviews")
                        .queryParam("bookingId", "111")
                        .build("123"))
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.reviewAccommodation())
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void cancelBooking() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setBookingId("111");
        book.setAccommodationId("123");
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/cancel", "111")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.userId", "andrea_damiani@protonmail.com").exists()
                .jsonPath("@.accommodationId", "123").exists()
                .jsonPath("@.bookingId", "111").exists()
                .jsonPath("@.status", "CANCELLED").exists();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setStatus(DONE);
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/cancel", "111")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void rescheduleBooking() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setStatus(CONFIRMED);
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setCheckin(LocalDate.now().plusDays(5));
        book.setCheckout(LocalDate.now().plusDays(10));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookingDates reschedule = PojoUtils.bookingDates();
        reschedule.setCheckin(LocalDate.now().plusDays(8));
        reschedule.setCheckout(LocalDate.now().plusDays(12));
        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(reschedule)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodationId", "123").exists()
                .jsonPath("@.bookingId", "111").exists()
                .jsonPath("@.checkin", reschedule.getCheckin()).exists()
                .jsonPath("@.checkout", reschedule.getCheckout()).exists()
                .jsonPath("@.status", "PENDING").exists();
    }

    @Test
    public void rescheduleBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setStatus(CANCELLED);
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setCheckin(LocalDate.now().plusDays(5));
        book.setCheckout(LocalDate.now().plusDays(10));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookingDates reschedule = PojoUtils.bookingDates();
        reschedule.setCheckin(LocalDate.now().plusDays(8));
        reschedule.setCheckout(LocalDate.now().plusDays(12));
        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(reschedule)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void rescheduleBooking_whenRescheduleHasSameDatesAsBooking_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = PojoUtils.booking();
        book.setUserId("andrea_damiani@protonmail.com");
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setCheckin(LocalDate.now().plusDays(5));
        book.setCheckout(LocalDate.now().plusDays(10));
        bookingRepo.save(book).block();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookingDates reschedule = PojoUtils.bookingDates();
        reschedule.setCheckin(LocalDate.now().plusDays(5));
        reschedule.setCheckout(LocalDate.now().plusDays(10));
        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(reschedule)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void rescheduleBooking_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book1 = PojoUtils.booking();
        book1.setUserId("andrea_damiani@protonmail.com");
        book1.setBookingId("111");
        book1.setAccommodationId("123");
        book1.setCheckin(LocalDate.now().plusDays(5));
        book1.setCheckout(LocalDate.now().plusDays(10));
        Booking book2 = PojoUtils.booking();
        book2.setUserId("andrea_damiani@protonmail.com");
        book2.setAccommodationId("456");
        book2.setCheckin(LocalDate.now().plusDays(20));
        book2.setCheckout(LocalDate.now().plusDays(22));
        bookingRepo.saveAll(Flux.just(book1, book2)).blockLast();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookingDates reschedule = PojoUtils.bookingDates();
        reschedule.setCheckin(LocalDate.now().plusDays(18));
        reschedule.setCheckout(LocalDate.now().plusDays(21));
        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(reschedule)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void rescheduleBooking_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book1 = PojoUtils.booking();
        book1.setUserId("andrea_damiani@protonmail.com");
        book1.setBookingId("111");
        book1.setAccommodationId("123");
        book1.setCheckin(LocalDate.now().plusDays(5));
        book1.setCheckout(LocalDate.now().plusDays(10));
        Booking book2 = PojoUtils.booking();
        book2.setAccommodationId("123");
        book2.setCheckin(LocalDate.now().plusDays(20));
        book2.setCheckout(LocalDate.now().plusDays(22));
        bookingRepo.saveAll(Flux.just(book1, book2)).blockLast();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        BookingDates reschedule = PojoUtils.bookingDates();
        reschedule.setCheckin(LocalDate.now().plusDays(18));
        reschedule.setCheckout(LocalDate.now().plusDays(21));
        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(reschedule)
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void getUserFavorites() {

        Accommodation acc1 = PojoUtils.accommodation();
        acc1.setAccommodationId("123");
        Accommodation acc2 = PojoUtils.accommodation();
        acc2.setAccommodationId("456");
        Accommodation acc3 = PojoUtils.accommodation();
        acc3.setAccommodationId("789");
        accommodationRepo.saveAll(Flux.just(acc1, acc2, acc3)).blockLast();

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .get()
                .uri("/api/v1/user-ops/my-favorites")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].accommodationId", "123").exists()
                .jsonPath("@.[1].accommodationId", "456").exists();
    }
}

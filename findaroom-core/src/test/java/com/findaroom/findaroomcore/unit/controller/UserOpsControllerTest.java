package com.findaroom.findaroomcore.unit.controller;

import com.findaroom.findaroomcore.config.SecurityConfig;
import com.findaroom.findaroomcore.controller.UserOpsController;
import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.dto.aggregates.BookingDetails;
import com.findaroom.findaroomcore.facade.UserOpsFacade;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.utils.JwtUtils;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.findaroom.findaroomcore.utils.JwtUtils.addJwt;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(UserOpsController.class)
@Import(SecurityConfig.class)
public class UserOpsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserOpsFacade userOps;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getUserBookings() {

        when(userOps.findBookingsByUserId(anyString(), any())).thenReturn(Flux.just(PojoUtils.booking()));

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

        when(userOps.findReviewsByUserId(anyString(), any())).thenReturn(Flux.just(PojoUtils.review()));

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
    public void getBookingDetails() {

        var details = new BookingDetails(PojoUtils.accommodation(), PojoUtils.booking());
        when(userOps.findBookingDetails(anyString(), anyString())).thenReturn(Mono.just(details));

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
                .jsonPath("@.accommodation").isNotEmpty()
                .jsonPath("@.booking").isNotEmpty();
    }

    @Test
    public void saveAccommodation() {

        when(userOps.saveAccommodation(anyString(), anyBoolean(), any())).thenReturn(Mono.just(PojoUtils.accommodation()));

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
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void saveAccommodation_whenBodyIsNotValid_shouldReturnBadRequest() {

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void bookAccommodation() {

        var details = new BookingDetails(PojoUtils.accommodation(), PojoUtils.booking());
        when(userOps.bookAccommodation(anyString(), anyString(), any())).thenReturn(Mono.just(details));

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
                .jsonPath("@.accommodation").isNotEmpty()
                .jsonPath("@.booking").isNotEmpty();
    }

    @Test
    public void bookAccommodation_whenBodyIsNotValid_shouldReturnBadRequest() {

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void reviewAccommodation() {

        when(userOps.reviewAccommodation(anyString(), anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.review()));

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
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void reviewAccommodation_whenBodyIsNotValid_shouldReturnBadRequest() {

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
                .bodyValue(new ReviewAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void cancelBooking() {

        when(userOps.cancelBooking(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/cancel", "123", "111")
                .headers(addJwt(jwt))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void rescheduleBooking() {

        when(userOps.rescheduleBooking(anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.booking()));

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "123", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.bookingDates())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void rescheduleBooking_whenBodyIsNotValid_shouldReturnBadRequest() {

        Jwt jwt = JwtUtils.jwt();
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        webTestClient
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "123", "111")
                .headers(addJwt(jwt))
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookingDates())
                .exchange()
                .expectStatus().isBadRequest();
    }
}

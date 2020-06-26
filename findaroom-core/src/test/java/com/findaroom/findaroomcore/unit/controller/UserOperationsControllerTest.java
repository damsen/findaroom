package com.findaroom.findaroomcore.unit.controller;

import com.findaroom.findaroomcore.config.SecurityConfig;
import com.findaroom.findaroomcore.controller.UserOperationsController;
import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.service.UserOperationsService;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(UserOperationsController.class)
@Import(SecurityConfig.class)
public class UserOperationsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserOperationsService userOps;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getUserBookings() {

        when(userOps.findBookingsByUserId(anyString(), any())).thenReturn(Flux.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/user-ops/my-bookings")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Booking.class).hasSize(1);
    }

    @Test
    public void getUserReviews() {

        when(userOps.findReviewsByUserId(anyString(), any())).thenReturn(Flux.just(PojoUtils.review()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/user-ops/my-reviews")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Review.class).hasSize(1);
    }

    @Test
    public void getUserFavorites() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(userOps.findUserFavorites(any(), any())).thenReturn(Flux.just(acc));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/user-ops/my-favorites")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].accommodationId", "123").exists();
    }

    @Test
    public void getUserBookingById() {

        when(userOps.findUserBookingById(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void saveAccommodation() {

        when(userOps.saveAccommodation(anyString(), anyBoolean(), any())).thenReturn(Mono.just(PojoUtils.accommodation()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt
                .claim("sub", "andrea_damiani@protonmail.com")
                .claim("superHost", false));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/user-ops/accommodations")
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

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/user-ops/accommodations")
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void bookAccommodation() {

        when(userOps.bookAccommodation(anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.bookAccommodation())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void bookAccommodation_whenBodyIsNotValid_shouldReturnBadRequest() {

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/user-ops/accommodations/{accommodationId}/bookings", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void reviewAccommodation() {

        when(userOps.reviewAccommodation(anyString(), anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.review()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/user-ops/accommodations/{accommodationId}/reviews")
                        .queryParam("bookingId", "111")
                        .build("123"))
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

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/user-ops/accommodations/{accommodationId}/reviews")
                        .queryParam("bookingId", "111")
                        .build("123"))
                .contentType(APPLICATION_JSON)
                .bodyValue(new ReviewAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void cancelBooking() {

        when(userOps.cancelBooking(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/cancel", "123", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void rescheduleBooking() {

        when(userOps.rescheduleBooking(anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "123", "111")
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

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/user-ops/my-bookings/{bookingId}/reschedule", "123", "111")
                .contentType(APPLICATION_JSON)
                .bodyValue(new BookingDates())
                .exchange()
                .expectStatus().isBadRequest();
    }
}

package com.findaroom.findaroomcore.unit.controller;

import com.findaroom.findaroomcore.config.SecurityConfig;
import com.findaroom.findaroomcore.controller.HostOperationsController;
import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.service.HostOperationsService;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(HostOperationsController.class)
@Import(SecurityConfig.class)
public class HostOperationsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HostOperationsService hostOps;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getHostAccommodations() {

        when(hostOps.findAccommodationsByHostId(anyString(), any())).thenReturn(Flux.just(PojoUtils.accommodation()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/host-ops/my-accommodations")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").value(hasSize(1))
                .jsonPath("@.[0]").isNotEmpty();
    }

    @Test
    public void getHostAccommodations_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/api/v1/host-ops/my-accommodations")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void getAccommodationBookings() {

        when(hostOps.findAccommodationBookingsByFilter(anyString(), anyString(), any())).thenReturn(Flux.just(PojoUtils.booking(), PojoUtils.booking()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Booking.class).hasSize(2);
    }

    @Test
    public void getAccommodationBookings_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings", "123")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void updateAccommodation() {

        when(hostOps.updateAccommodation(anyString(), anyString(), any())).thenReturn(Mono.just(PojoUtils.accommodation()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.updateAccommodation())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void updateAccommodation_whenBodyIsNotValid_shouldReturnBadRequest() {

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UpdateAccommodation())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateAccommodation_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(PojoUtils.updateAccommodation())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void confirmBooking() {

        when(hostOps.confirmBooking(anyString(), anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/confirm", "123", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void confirmBooking_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/confirm", "123", "111")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void cancelBooking() {

        when(hostOps.cancelBooking(anyString(), anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/cancel", "123", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void cancelBooking_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/cancel", "123", "111")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void unlistAccommodation() {

        when(hostOps.unlistAccommodation(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));

        var jwtMutator = mockJwt().authorities(new SimpleGrantedAuthority("host"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/unlist", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void unlistAccommodation_whenNoHostAuthority_shouldReturnForbidden() {

        webTestClient
                .mutateWith(mockJwt())
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/unlist", "123")
                .exchange()
                .expectStatus().isForbidden();
    }
}

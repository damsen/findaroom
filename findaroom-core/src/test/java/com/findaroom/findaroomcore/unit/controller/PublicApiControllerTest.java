package com.findaroom.findaroomcore.unit.controller;

import com.findaroom.findaroomcore.config.SecurityConfig;
import com.findaroom.findaroomcore.controller.PublicApiController;
import com.findaroom.findaroomcore.dto.aggregates.AccommodationDetails;
import com.findaroom.findaroomcore.facade.PublicApiFacade;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.utils.PojoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.findaroom.findaroomcore.utils.PojoUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(PublicApiController.class)
@Import(SecurityConfig.class)
public class PublicApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PublicApiFacade publicApi;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getAccommodations() {

        when(publicApi.findAccommodationsByFilter(any())).thenReturn(Flux.just(accommodation(), accommodation()));

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0]").isNotEmpty()
                .jsonPath("@.[1]").isNotEmpty();
    }

    @Test
    public void getAccommodationDetails() {

        var details = new AccommodationDetails(accommodation(), List.of(review(), review()));
        when(publicApi.findAccommodationDetails("123")).thenReturn(Mono.just(details));

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations/{accommodationId}", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodation").isNotEmpty()
                .jsonPath("@.reviews").isNotEmpty();
    }

    @Test
    public void getAccommodationReviews() {

        when(publicApi.findAccommodationReviewsByFilter(anyString(), any())).thenReturn(Flux.just(review(), review()));

        webTestClient
                .get()
                .uri("/api/v1/public/accommodations/{accommodationId}/reviews", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Review.class).hasSize(2);

    }
}

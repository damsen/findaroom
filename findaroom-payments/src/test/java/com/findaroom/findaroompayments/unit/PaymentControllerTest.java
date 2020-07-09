package com.findaroom.findaroompayments.unit;

import com.findaroom.findaroompayments.config.SecurityConfig;
import com.findaroom.findaroompayments.payment.Checkout;
import com.findaroom.findaroompayments.payment.PaymentService;
import com.findaroom.findaroompayments.utils.TestPojos;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest
@Import(SecurityConfig.class)
public class PaymentControllerTest {

    @MockBean
    public PaymentService paymentService;

    @Autowired
    public WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    public void getById() {

        when(paymentService.findByPaymentIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.payment()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/payments/{paymentId}", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void checkout() {

        when(paymentService.checkout(anyString(), anyString(), anyString())).thenReturn(Mono.just(TestPojos.payment()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/payments")
                .contentType(APPLICATION_JSON)
                .bodyValue(new Checkout("orderId", "bookingId"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@").isNotEmpty();
    }

    @Test
    public void checkout_whenBodyIsNotValid_shouldReturnBadRequest() {

        when(paymentService.checkout(anyString(), anyString(), anyString())).thenReturn(Mono.just(TestPojos.payment()));

        var jwtMutator = mockJwt().jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .post()
                .uri("/api/v1/payments")
                .contentType(APPLICATION_JSON)
                .bodyValue(new Checkout("", ""))
                .exchange()
                .expectStatus().isBadRequest();
    }

}

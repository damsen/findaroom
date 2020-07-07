package com.findaroom.findaroompayments.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public Mono<Payment> getById(@PathVariable String paymentId,
                                 @AuthenticationPrincipal Jwt jwt) {
        return paymentService.findByPaymentIdAndUserId(paymentId, jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<Payment> checkout(@RequestBody @Valid Checkout checkout,
                                  @AuthenticationPrincipal Jwt jwt) {
        return paymentService.checkout(jwt.getSubject(), checkout.getOrderId(), checkout.getBookingId());
    }
}

package com.findaroom.findaroompayments.unit;

import com.findaroom.findaroompayments.payment.Payment;
import com.findaroom.findaroompayments.payment.PaymentRepository;
import com.findaroom.findaroompayments.payment.PaymentService;
import com.findaroom.findaroompayments.payment.ReactivePayPalClient;
import com.findaroom.findaroompayments.utils.TestPojos;
import com.findaroom.findaroompayments.utils.TestPredicates;
import com.paypal.orders.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.findaroom.findaroompayments.utils.MessageUtils.*;
import static com.findaroom.findaroompayments.utils.TestPredicates.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentServiceTest {

    @MockBean
    private PaymentRepository paymentRepo;

    @MockBean
    private ReactivePayPalClient payPalClient;

    private PaymentService paymentService;

    @BeforeAll
    public void setup() {
        paymentService = new PaymentService(paymentRepo, payPalClient);
    }

    @Test
    public void findByPaymentIdAndUserId() {

        when(paymentRepo.findByPaymentIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.payment()));

        Mono<Payment> payment = paymentService.findByPaymentIdAndUserId("999", "444");

        StepVerifier
                .create(payment)
                .assertNext(p -> Assertions.assertThat(p).isNotNull())
                .verifyComplete();
    }

    @Test
    public void findByPaymentIdAndUserId_whenPaymentNotFound_shouldReturnNotFound() {

        when(paymentRepo.findByPaymentIdAndUserId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Payment> payment = paymentService.findByPaymentIdAndUserId("999", "444");

        StepVerifier
                .create(payment)
                .expectErrorMatches(TestPredicates.notFound(ORDER_NOT_FOUND))
                .verify();
    }

    @Test
    public void checkout() {

        Payment pay = TestPojos.payment();
        when(payPalClient.getOrderById(anyString())).thenReturn(Mono.just(TestPojos.order()));
        when(paymentRepo.save(any())).thenReturn(Mono.just(pay));

        Mono<Payment> payment = paymentService.checkout("123", "abc", "111");

        StepVerifier
                .create(payment)
                .expectNext(pay)
                .verifyComplete();
    }

    @Test
    public void checkout_whenOrderNotCompleted_shouldReturnUnprocessableEntity() {

        Order order = TestPojos.order();
        order.status("SOMETHING_ELSE");
        when(payPalClient.getOrderById(anyString())).thenReturn(Mono.just(order));
        when(paymentRepo.save(any())).thenReturn(Mono.just(TestPojos.payment()));

        Mono<Payment> payment = paymentService.checkout("123", "abc", "111");

        StepVerifier
                .create(payment)
                .expectErrorMatches(unprocessableEntity(ORDER_WAS_NOT_COMPLETED))
                .verify();
    }

    @Test
    public void checkout_whenOrderIsCorrupted_shouldReturnInternalServerError() {

        Order order = TestPojos.order();
        order.status("COMPLETED");
        order.purchaseUnits()
                .stream()
                .findFirst()
                .map(PurchaseUnit::payments)
                .ifPresent(paymentCollection -> paymentCollection.captures(List.of()));

        when(payPalClient.getOrderById(anyString())).thenReturn(Mono.just(order));
        when(paymentRepo.save(any())).thenReturn(Mono.just(TestPojos.payment()));

        Mono<Payment> payment = paymentService.checkout("123", "abc", "111");

        StepVerifier
                .create(payment)
                .expectErrorMatches(internalServerError(ORDER_IS_CORRUPTED))
                .verify();
    }
}

package com.findaroom.findaroompayments.payment;

import com.paypal.orders.Capture;
import com.paypal.orders.Order;
import com.paypal.orders.PaymentCollection;
import com.paypal.orders.PurchaseUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.findaroom.findaroompayments.utils.ErrorUtils.Suppliers.*;
import static com.findaroom.findaroompayments.utils.MessageUtils.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public static final String COMPLETED = "COMPLETED";

    private final PaymentRepository paymentRepo;
    private final ReactivePayPalClient payPalClient;

    public Mono<Payment> findByPaymentIdAndUserId(String paymentId, String userId) {
        return paymentRepo
                .findByPaymentIdAndUserId(paymentId, userId)
                .switchIfEmpty(Mono.error(notFound(PAYMENT_NOT_FOUND)));
    }

    public Mono<Payment> checkout(String userId, String orderId, String bookingId) {
        return payPalClient
                .getOrderById(orderId)
                .filter(order -> Objects.equals(order.status(), COMPLETED))
                .switchIfEmpty(Mono.error(unprocessableEntity(ORDER_WAS_NOT_COMPLETED)))
                .flatMap(order -> captureOf(order).map(capture -> Payment.of(userId, orderId, bookingId, capture.amount())))
                .flatMap(paymentRepo::save);
    }

    private Mono<Capture> captureOf(Order order) {
        return Flux.fromIterable(order.purchaseUnits())
                .next()
                .map(PurchaseUnit::payments)
                .map(PaymentCollection::captures)
                .flatMapMany(Flux::fromIterable)
                .next()
                .switchIfEmpty(Mono.error(internalServerError(ORDER_IS_CORRUPTED)));
    }
}

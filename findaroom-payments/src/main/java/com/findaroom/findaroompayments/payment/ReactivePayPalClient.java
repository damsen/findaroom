package com.findaroom.findaroompayments.payment;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersGetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.findaroom.findaroompayments.utils.ErrorUtils.internalServerError;

@Component
@RequiredArgsConstructor
public class ReactivePayPalClient {

    private final PayPalHttpClient httpClient;

    public Mono<Order> getOrderById(String orderId) {
        return Mono.fromCallable(() -> httpClient.execute(new OrdersGetRequest(orderId)))
                .map(HttpResponse::result)
                .onErrorMap(e -> internalServerError(e.getLocalizedMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

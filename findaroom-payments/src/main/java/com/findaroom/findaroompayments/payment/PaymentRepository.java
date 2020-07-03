package com.findaroom.findaroompayments.payment;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

    Mono<Payment> findByPaymentIdAndUserId(String paymentId, String userId);

}

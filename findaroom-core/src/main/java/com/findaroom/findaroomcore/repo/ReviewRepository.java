package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.model.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReviewRepository extends ReactiveMongoRepository<Review, String>, CustomReviewRepository {

    Mono<Boolean> existsByBookingId(String bookingId);

}

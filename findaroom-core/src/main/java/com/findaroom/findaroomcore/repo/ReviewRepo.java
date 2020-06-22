package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.model.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReviewRepo extends ReactiveMongoRepository<Review, String>, CustomReviewRepo {

    Mono<Boolean> existsByBookingId(String bookingId);

}

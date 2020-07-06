package com.findaroom.findaroomcore.repository;

import com.findaroom.findaroomcore.controller.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.domain.Review;
import reactor.core.publisher.Flux;

public interface CustomReviewRepository {

    Flux<Review> findAllByFilter(ReviewSearchFilter filter);
}

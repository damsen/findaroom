package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Review;
import reactor.core.publisher.Flux;

public interface CustomReviewRepository {

    Flux<Review> findAllByFilter(ReviewSearchFilter filter);
}

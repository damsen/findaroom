package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Review;
import reactor.core.publisher.Flux;

public interface CustomReviewRepo {

    Flux<Review> findAllByFilter(ReviewSearchFilter filter);
}

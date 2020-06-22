package com.findaroom.findaroomcore.facade;

import com.findaroom.findaroomcore.dto.aggregates.AccommodationDetails;
import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Review;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PublicApiFacade {

    public Flux<Accommodation> findAccommodationsByFilter(AccommodationSearchFilter filter) {
        return null;
    }

    public Mono<AccommodationDetails> findAccommodationDetails(String accommodationId) {
        return null;
    }

    public Flux<Review> findAccommodationReviewsByFilter(String accommodationId, ReviewSearchFilter filter) {
        return null;
    }
}

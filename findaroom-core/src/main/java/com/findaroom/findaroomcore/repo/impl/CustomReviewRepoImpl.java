package com.findaroom.findaroomcore.repo.impl;

import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.CustomReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class CustomReviewRepoImpl implements CustomReviewRepo {

    private final ReactiveMongoOperations mongoOps;

    @Override
    public Flux<Review> findAllByFilter(ReviewSearchFilter filter) {

        var query = new Query();

        return convertToCriteria(filter)
                .map(query::addCriteria)
                .then(filter.getPageable())
                .map(query::with)
                .flatMapMany(q -> mongoOps.find(q, Review.class));
    }

    private Flux<CriteriaDefinition> convertToCriteria(ReviewSearchFilter params) {

        var accommodationFilter = params.getAccommodationId().map(where("accommodationId")::is);
        var userFilter = params.getUserId().map(where("userId")::is);
        var bookingFilter = params.getBookingId().map(where("bookingId")::is);
        var ratingFilter = params.getRating().map(where("rating")::gte);
        var textFilter = params.getQ()
                .map(words -> TextCriteria.forDefaultLanguage()
                        .matchingAny(words.split(" "))
                        .caseSensitive(false));

        return Flux.merge(accommodationFilter, userFilter, bookingFilter, ratingFilter, textFilter);
    }
}

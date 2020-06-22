package com.findaroom.findaroomcore.repo.impl;

import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.repo.CustomBookingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class CustomBookingRepoImpl implements CustomBookingRepo {

    private final ReactiveMongoOperations mongoOps;

    @Override
    public Flux<Booking> findAllByFilter(BookingSearchFilter filter) {

        var query = new Query();

        return toCriteria(filter)
                .map(query::addCriteria)
                .then(filter.getPageable())
                .map(query::with)
                .flatMapMany(q -> mongoOps.find(q, Booking.class));
    }

    private Flux<CriteriaDefinition> toCriteria(BookingSearchFilter params) {

        var accommodationFilter = params.getAccommodationId().map(where("accommodationId")::is);
        var userFilter = params.getUserId().map(where("userId")::is);
        var statusFilter = params.getStatus().map(where("status")::in);

        var checkin = params.getCheckin().map(where("checkout")::gte).cast(Criteria.class);
        var checkout = params.getCheckout().map(where("checkin")::lte).cast(Criteria.class);
        var betweenFilter = Mono.zip(checkin, checkout, Criteria::andOperator);

        return Flux.merge(accommodationFilter, userFilter, statusFilter, betweenFilter);
    }
}

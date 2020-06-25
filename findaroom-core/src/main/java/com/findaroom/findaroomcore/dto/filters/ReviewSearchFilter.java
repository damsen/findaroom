package com.findaroom.findaroomcore.dto.filters;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewSearchFilter extends PagingAndSortingFilter {

    String accommodationId;
    String userId;
    String bookingId;
    Double rating;
    String q;

    public Mono<String> getAccommodationId() {
        return Mono.justOrEmpty(accommodationId);
    }

    public Mono<String> getUserId() {
        return Mono.justOrEmpty(userId);
    }

    public Mono<String> getBookingId() {
        return Mono.justOrEmpty(bookingId);
    }

    public Mono<Double> getRating() {
        return Mono.justOrEmpty(rating);
    }

    public Mono<String> getQ() {
        return Mono.justOrEmpty(q);
    }
}

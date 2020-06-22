package com.findaroom.findaroomcore.controller;

import com.findaroom.findaroomcore.dto.aggregates.AccommodationDetails;
import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.facade.PublicApiFacade;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final PublicApiFacade publicApi;

    @GetMapping("/accommodations")
    public Flux<Accommodation> getAccommodations(AccommodationSearchFilter filter) {
        return publicApi.findAccommodationsByFilter(filter);
    }

    @GetMapping("/accommodations/{accommodationId}")
    public Mono<AccommodationDetails> getAccommodationDetails(@PathVariable String accommodationId) {
        return publicApi.findAccommodationDetails(accommodationId);
    }

    @GetMapping("/accommodations/{accommodationId}/reviews")
    public Flux<Review> getAccommodationReviews(@PathVariable String accommodationId,
                                                ReviewSearchFilter filter) {
        return publicApi.findAccommodationReviewsByFilter(accommodationId, filter);
    }
}

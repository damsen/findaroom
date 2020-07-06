package com.findaroom.findaroomcore.controller;

import com.findaroom.findaroomcore.controller.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.controller.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.domain.Accommodation;
import com.findaroom.findaroomcore.domain.Review;
import com.findaroom.findaroomcore.service.PublicApiService;
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

    private final PublicApiService publicApi;

    @GetMapping("/accommodations")
    public Flux<Accommodation> getAccommodations(AccommodationSearchFilter filter) {
        return publicApi.findAccommodationsByFilter(filter);
    }

    @GetMapping("/accommodations/{accommodationId}")
    public Mono<Accommodation> getAccommodationById(@PathVariable String accommodationId) {
        return publicApi.findAccommodationById(accommodationId);
    }

    @GetMapping("/accommodations/{accommodationId}/reviews")
    public Flux<Review> getAccommodationReviews(@PathVariable String accommodationId,
                                                ReviewSearchFilter filter) {
        return publicApi.findAccommodationReviewsByFilter(accommodationId, filter);
    }
}

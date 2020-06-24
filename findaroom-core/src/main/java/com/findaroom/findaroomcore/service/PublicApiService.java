package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepo;
import com.findaroom.findaroomcore.repo.BookingRepo;
import com.findaroom.findaroomcore.repo.ReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final AccommodationRepo accommodationRepo;
    private final BookingRepo bookingRepo;
    private final ReviewRepo reviewRepo;

    public Flux<Accommodation> findAccommodationsByFilter(AccommodationSearchFilter filter) {

        var bookedAccommodationIds = filter
                .getBookingDates()
                .map(BookingSearchFilter::new)
                .flatMapMany(bookingRepo::findAllByFilter)
                .map(Booking::getAccommodationId)
                .distinct()
                .collectList();

        return bookedAccommodationIds
                .doOnNext(filter::setExclude)
                .then(Mono.just(filter))
                .flatMapMany(accommodationRepo::findAllByFilter);
    }

    public Mono<Accommodation> findAccommodationById(String accommodationId) {
        return accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)));
    }

    public Flux<Review> findAccommodationReviewsByFilter(String accommodationId, ReviewSearchFilter filter) {
        return accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .doOnNext(accommodation -> filter.setAccommodationId(accommodation.getAccommodationId()))
                .then(Mono.just(filter))
                .flatMapMany(reviewRepo::findAllByFilter);
    }
}

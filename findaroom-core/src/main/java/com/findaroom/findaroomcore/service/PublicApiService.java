package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.controller.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.controller.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.controller.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.domain.Accommodation;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.Review;
import com.findaroom.findaroomcore.repository.AccommodationRepository;
import com.findaroom.findaroomcore.repository.BookingRepository;
import com.findaroom.findaroomcore.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.findaroom.findaroomcore.utils.ErrorUtils.notFound;
import static com.findaroom.findaroomcore.utils.MessageUtils.ACCOMMODATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final AccommodationRepository accommodationRepo;
    private final BookingRepository bookingRepo;
    private final ReviewRepository reviewRepo;

    public Flux<Accommodation> findAccommodationsByFilter(AccommodationSearchFilter filter) {
        return filter
                .getBookingDates()
                .map(BookingSearchFilter::new)
                .flatMapMany(bookingRepo::findAllByFilter)
                .map(Booking::getAccommodationId)
                .distinct()
                .collectList()
                .doOnNext(filter::setExclude)
                .then(Mono.just(filter))
                .flatMapMany(accommodationRepo::findAllByFilter);
    }

    public Mono<Accommodation> findAccommodationById(String accommodationId) {
        return accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)));
    }

    public Flux<Review> findAccommodationReviewsByFilter(String accommodationId, ReviewSearchFilter filter) {
        return accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                .doOnNext(accommodation -> filter.setAccommodationId(accommodation.getAccommodationId()))
                .then(Mono.just(filter))
                .flatMapMany(reviewRepo::findAllByFilter);
    }
}

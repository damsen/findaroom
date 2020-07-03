package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
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

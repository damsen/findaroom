package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.service.verifier.AccommodationVerifier;
import com.findaroom.findaroomcore.service.verifier.BookingVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.activeStates;
import static com.findaroom.findaroomcore.utils.ErrorUtils.notFound;
import static com.findaroom.findaroomcore.utils.ErrorUtils.unprocessableEntity;
import static com.findaroom.findaroomcore.utils.MessageUtils.*;

@Service
@RequiredArgsConstructor
public class UserOperationsService {

    private final AccommodationRepository accommodationRepo;
    private final BookingRepository bookingRepo;
    private final ReviewRepository reviewRepo;
    private final AccommodationVerifier accommodationVerifier;
    private final BookingVerifier bookingVerifier;

    public Flux<Booking> findBookingsByUserId(String userId, BookingSearchFilter filter) {
        filter.setUserId(userId);
        return bookingRepo.findAllByFilter(filter);
    }

    public Flux<Review> findReviewsByUserId(String userId, ReviewSearchFilter filter) {
        filter.setUserId(userId);
        return reviewRepo.findAllByFilter(filter);
    }

    public Flux<Accommodation> findUserFavorites(List<String> favoriteIds, AccommodationSearchFilter filter) {
        filter.setSelect(favoriteIds);
        return accommodationRepo.findAllByFilter(filter);
    }

    public Mono<Booking> findUserBookingById(String bookingId, String userId) {
        return bookingRepo
                .findByBookingIdAndUserId(bookingId, userId)
                .switchIfEmpty(Mono.error(notFound(BOOKING_NOT_FOUND)));
    }

    public Mono<Accommodation> saveAccommodation(String userId, boolean superHost, CreateAccommodation create) {
        return accommodationRepo.save(Accommodation.from(userId, superHost, create));
    }

    public Mono<Booking> bookAccommodation(String accommodationId, String userId, BookAccommodation book) {

        var accommodationById = accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                .flatMap(accommodation -> accommodationVerifier.verifyUserIsNotAccommodationHost(accommodation, userId))
                .flatMap(accommodation -> accommodationVerifier.verifyGuestsDoNotExceedCapacity(accommodation, book.getGuests()))
                .flatMap(accommodation -> accommodationVerifier.verifyAccommodationIsAvailable(accommodation, book.getBookingDates()));

        var userIsAvailableBetweenDates = bookingRepo
                .countActiveUserBookingsBetweenDates(userId, book.getBookingDates().getCheckin(), book.getBookingDates().getCheckout(), activeStates())
                .filter(count -> count == 0)
                .switchIfEmpty(Mono.error(unprocessableEntity(USER_HAS_BOOKINGS_BETWEEN_DATES)));

        return Mono.zip(accommodationById, userIsAvailableBetweenDates)
                .flatMap(t -> bookingRepo.save(Booking.from(accommodationId, userId, book)));
    }

    public Mono<Review> reviewAccommodation(String accommodationId, String bookingId, String userId, ReviewAccommodation review) {

        var accommodationById = accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)));

        var bookingById = bookingRepo
                .findByBookingIdAndAccommodationIdAndUserId(bookingId, accommodationId, userId)
                .switchIfEmpty(Mono.error(notFound(BOOKING_NOT_FOUND)))
                .flatMap(bookingVerifier::verifyBookingIsCompleted);

        return Mono.zip(accommodationById, bookingById)
                .map(Tuple2::getT1)
                .flatMap(accommodation -> reviewAccommodationInternal(accommodation, Review.from(accommodationId, userId, bookingId, review)));
    }

    public Mono<Booking> cancelBooking(String bookingId, String userId) {
        return bookingRepo
                .findByBookingIdAndUserId(bookingId, userId)
                .switchIfEmpty(Mono.error(notFound(BOOKING_NOT_FOUND)))
                .flatMap(bookingVerifier::verifyBookingIsActive)
                .doOnNext(booking -> booking.setStatus(CANCELLED))
                .flatMap(bookingRepo::save);
    }

    public Mono<Booking> rescheduleBooking(String bookingId, String userId, BookingDates dates) {

        var bookingById = bookingRepo
                .findByBookingIdAndUserId(bookingId, userId)
                .switchIfEmpty(Mono.error(notFound(BOOKING_NOT_FOUND)))
                .flatMap(bookingVerifier::verifyBookingIsActive)
                .flatMap(booking -> bookingVerifier.verifyBookingHasDifferentDatesThan(booking, dates))
                .flatMap(booking -> accommodationRepo
                        .findById(booking.getAccommodationId())
                        .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                        .flatMap(accommodation -> accommodationVerifier.verifyAccommodationIsAvailableExcludingBooking(accommodation, booking.getBookingId(), dates))
                        .thenReturn(booking));

        var userIsAvailableBetweenDates = bookingRepo
                .countActiveUserBookingsBetweenDatesExcludingBooking(userId, bookingId, dates.getCheckin(), dates.getCheckout(), activeStates())
                .filter(count -> count == 0)
                .switchIfEmpty(Mono.error(unprocessableEntity(USER_HAS_BOOKINGS_BETWEEN_DATES)));

        return Mono.zip(bookingById, userIsAvailableBetweenDates)
                .map(Tuple2::getT1)
                .doOnNext(booking -> booking.rescheduleWith(dates))
                .flatMap(bookingRepo::save);
    }

    private Mono<Review> reviewAccommodationInternal(Accommodation accommodation, Review review) {
        return reviewRepo.save(review)
                .flatMap(saved -> calculateNewAverageRating(accommodation.getAccommodationId())
                        .doOnNext(accommodation::setRating)
                        .then(accommodationRepo.save(accommodation))
                        .thenReturn(saved));
    }

    private Mono<Double> calculateNewAverageRating(String accommodationId) {
        var filter = new ReviewSearchFilter();
        filter.setAccommodationId(accommodationId);
        return reviewRepo
                .findAllByFilter(filter)
                .collect(Collectors.averagingDouble(Review::getRating));
    }
}

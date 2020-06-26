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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.activeStates;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
@RequiredArgsConstructor
public class UserOperationsService {

    private final AccommodationRepository accommodationRepo;
    private final BookingRepository bookingRepo;
    private final ReviewRepository reviewRepo;

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
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)));
    }

    public Mono<Accommodation> saveAccommodation(String userId, boolean superHost, CreateAccommodation create) {
        return accommodationRepo.save(Accommodation.from(userId, superHost, create));
    }

    public Mono<Booking> bookAccommodation(String accommodationId, String userId, BookAccommodation book) {

        var accommodationById = accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(accommodation -> !accommodation.hasHostWithId(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .filter(accommodation -> accommodation.fitsGuests(book.getGuests()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)));

        var userHasBookingsBetweenDates = doesUserHaveBookingsBetweenDates(userId, book.getBookingDates())
                .filter(hasBookings -> !hasBookings)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)));

        var accommodationAlreadyBooked = isAccommodationBookedBetweenDates(accommodationId, book.getBookingDates())
                .filter(alreadyBooked -> !alreadyBooked)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)));

        return Mono.zip(accommodationById, userHasBookingsBetweenDates, accommodationAlreadyBooked)
                .flatMap(t -> bookingRepo.save(Booking.from(accommodationId, userId, book)));
    }

    public Mono<Review> reviewAccommodation(String accommodationId, String bookingId, String userId, ReviewAccommodation review) {

        var accommodationById = accommodationRepo
                .findById(accommodationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)));

        var bookingById = bookingRepo
                .findByBookingIdAndAccommodationIdAndUserId(bookingId, accommodationId, userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(Booking::isCompleted)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)));

        return Mono.zip(accommodationById, bookingById)
                .map(Tuple2::getT1)
                .flatMap(accommodation -> reviewAccommodationInternal(accommodation, Review.from(accommodationId, userId, bookingId, review)));
    }

    public Mono<Booking> cancelBooking(String bookingId, String userId) {
        return bookingRepo
                .findByBookingIdAndUserId(bookingId, userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(Booking::isActive)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .doOnNext(booking -> booking.setStatus(CANCELLED))
                .flatMap(bookingRepo::save);
    }

    public Mono<Booking> rescheduleBooking(String bookingId, String userId, BookingDates reschedule) {
        return bookingRepo
                .findByBookingIdAndUserId(bookingId, userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(Booking::isActive)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .filter(booking -> booking.hasDifferentDatesThan(reschedule))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .filterWhen(booking -> doesUserHaveBookingsBetweenDatesExcludingBooking(userId, booking.getBookingId(), reschedule)
                        .map(hasBookings -> !hasBookings))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .filterWhen(booking -> isAccommodationBookedExcludingBooking(booking.getAccommodationId(), booking.getBookingId(), reschedule)
                        .map(alreadyBooked -> !alreadyBooked))
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
                .doOnNext(booking -> booking.rescheduleWith(reschedule))
                .flatMap(bookingRepo::save);
    }

    private Mono<Boolean> doesUserHaveBookingsBetweenDates(String userId, BookingDates dates) {
        return bookingRepo
                .countActiveUserBookingsBetweenDates(userId, dates.getCheckin(), dates.getCheckout(), activeStates())
                .map(count -> count > 0);
    }

    private Mono<Boolean> isAccommodationBookedBetweenDates(String accommodationId, BookingDates dates) {
        return bookingRepo
                .countActiveAccommodationBookingsBetweenDates(accommodationId, dates.getCheckin(), dates.getCheckout(), activeStates())
                .map(count -> count > 0);
    }

    private Mono<Boolean> doesUserHaveBookingsBetweenDatesExcludingBooking(String userId, String bookingId, BookingDates dates) {
        return bookingRepo
                .countActiveUserBookingsBetweenDatesExcludingBooking(userId, bookingId, dates.getCheckin(), dates.getCheckout(), activeStates())
                .map(count -> count > 0);
    }

    private Mono<Boolean> isAccommodationBookedExcludingBooking(String accommodationId, String bookingId, BookingDates dates) {
        return bookingRepo
                .countActiveAccommodationBookingsBetweenDatesExcludingBooking(accommodationId, bookingId, dates.getCheckin(), dates.getCheckout(), activeStates())
                .map(count -> count > 0);
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

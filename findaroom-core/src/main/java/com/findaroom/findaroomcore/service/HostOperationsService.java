package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.controller.event.UpdateAccommodation;
import com.findaroom.findaroomcore.controller.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.controller.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.domain.Accommodation;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
import com.findaroom.findaroomcore.repository.AccommodationRepository;
import com.findaroom.findaroomcore.repository.BookingRepository;
import com.findaroom.findaroomcore.service.validation.AccommodationVerifier;
import com.findaroom.findaroomcore.service.validation.BookingVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.findaroom.findaroomcore.domain.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.domain.enums.BookingStatus.CONFIRMED;
import static com.findaroom.findaroomcore.utils.ErrorUtils.notFound;
import static com.findaroom.findaroomcore.utils.MessageUtils.ACCOMMODATION_NOT_FOUND;
import static com.findaroom.findaroomcore.utils.MessageUtils.BOOKING_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HostOperationsService {

    private final AccommodationRepository accommodationRepo;
    private final BookingRepository bookingRepo;
    private final AccommodationVerifier accommodationVerifier;
    private final BookingVerifier bookingVerifier;

    public Flux<Accommodation> findAccommodationsByHostId(String hostId, AccommodationSearchFilter filter) {
        filter.setHostId(hostId);
        return accommodationRepo.findAllByFilter(filter);
    }

    public Flux<Booking> findAccommodationBookingsByFilter(String accommodationId, String hostId, BookingSearchFilter filter) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                .doOnNext(accommodation -> filter.setAccommodationId(accommodation.getAccommodationId()))
                .then(Mono.just(filter))
                .flatMapMany(bookingRepo::findAllByFilter);
    }

    public Mono<Accommodation> updateAccommodation(String accommodationId, String hostId, UpdateAccommodation update) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                .map(accommodation -> accommodation.updateWith(update))
                .flatMap(accommodationRepo::save);
    }

    public Mono<Booking> confirmBooking(String accommodationId, String bookingId, String hostId) {
        return updateBookingStatus(accommodationId, bookingId, hostId,
                bookingVerifier::verifyBookingIsPending, booking -> booking.setStatus(CONFIRMED));
    }

    public Mono<Booking> cancelBooking(String accommodationId, String bookingId, String hostId) {
        return updateBookingStatus(accommodationId, bookingId, hostId,
                bookingVerifier::verifyBookingIsActive, booking -> booking.setStatus(CANCELLED));
    }

    public Mono<Accommodation> unlistAccommodation(String accommodationId, String hostId) {
        return unlistAccommodationInternal(accommodationId, hostId)
                .flatMap(unlisted -> cancelAllBookings(accommodationId).thenReturn(unlisted));
    }

    private Mono<Booking> updateBookingStatus(String accommodationId, String bookingId, String hostId,
                                              Function<Booking, Mono<Booking>> verifyStatus, Consumer<Booking> changeStatus) {

        var accommodationById = accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)));

        var bookingById = bookingRepo
                .findByBookingIdAndAccommodationId(bookingId, accommodationId)
                .switchIfEmpty(Mono.error(notFound(BOOKING_NOT_FOUND)))
                .flatMap(verifyStatus);

        return Mono.zip(accommodationById, bookingById)
                .map(Tuple2::getT2)
                .doOnNext(changeStatus)
                .flatMap(bookingRepo::save);
    }

    private Mono<Accommodation> unlistAccommodationInternal(String accommodationId, String hostId) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(notFound(ACCOMMODATION_NOT_FOUND)))
                .flatMap(accommodationVerifier::verifyAccommodationIsListed)
                .doOnNext(listed -> listed.setListed(false))
                .flatMap(accommodationRepo::save);
    }

    private Mono<List<Booking>> cancelAllBookings(String accommodationId) {
        var filter = new BookingSearchFilter();
        filter.setAccommodationId(accommodationId);
        filter.setStatus(BookingStatus.activeStates());
        return bookingRepo
                .findAllByFilter(filter)
                .doOnNext(booking -> booking.setStatus(CANCELLED))
                .collectList()
                .flatMapMany(bookingRepo::saveAll)
                .collectList();
    }
}

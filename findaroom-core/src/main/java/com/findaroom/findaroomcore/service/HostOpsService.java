package com.findaroom.findaroomcore.service;

import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.enums.BookingStatus;
import com.findaroom.findaroomcore.repo.AccommodationRepo;
import com.findaroom.findaroomcore.repo.BookingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.CONFIRMED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
@RequiredArgsConstructor
public class HostOpsService {

    private final AccommodationRepo accommodationRepo;
    private final BookingRepo bookingRepo;

    public Flux<Accommodation> findAccommodationsByHostId(String hostId, AccommodationSearchFilter filter) {
        filter.setHostId(hostId);
        return accommodationRepo.findAllByFilter(filter);
    }

    public Flux<Booking> findAccommodationBookingsByFilter(String accommodationId, String hostId, BookingSearchFilter filter) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .doOnNext(accommodation -> filter.setAccommodationId(accommodation.getAccommodationId()))
                .then(Mono.just(filter))
                .flatMapMany(bookingRepo::findAllByFilter);
    }

    public Mono<Accommodation> updateAccommodation(String accommodationId, String hostId, UpdateAccommodation update) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .map(accommodation -> accommodation.updateWith(update))
                .flatMap(accommodationRepo::save);
    }

    public Mono<Booking> confirmBooking(String accommodationId, String bookingId, String hostId) {
        return updateBookingStatus(accommodationId, bookingId, hostId,
                Booking::isPending, booking -> booking.setStatus(CONFIRMED));
    }

    public Mono<Booking> cancelBooking(String accommodationId, String bookingId, String hostId) {
        return updateBookingStatus(accommodationId, bookingId, hostId,
                Booking::isActive, booking -> booking.setStatus(CANCELLED));
    }

    public Mono<Accommodation> unlistAccommodation(String accommodationId, String hostId) {

        var unlisted = unlistAccommodationInternal(accommodationId, hostId);
        var cancelled = cancelAllBookings(accommodationId);

        return unlisted.flatMap(cancelled::thenReturn);
    }

    private Mono<Booking> updateBookingStatus(String accommodationId, String bookingId, String hostId,
                              Predicate<Booking> filterByStatus, Consumer<Booking> changeStatus) {

        var accommodationById = accommodationRepo.findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)));

        var bookingById = bookingRepo.findByBookingIdAndAccommodationId(bookingId, accommodationId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(filterByStatus)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)));

        return Mono.zip(accommodationById, bookingById)
                .map(Tuple2::getT2)
                .doOnNext(changeStatus)
                .flatMap(bookingRepo::save);
    }

    private Mono<Accommodation> unlistAccommodationInternal(String accommodationId, String hostId) {
        return accommodationRepo
                .findByAccommodationIdAndHostId(accommodationId, hostId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .filter(Accommodation::isListed)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY)))
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

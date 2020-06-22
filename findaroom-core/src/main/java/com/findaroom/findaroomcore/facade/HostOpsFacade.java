package com.findaroom.findaroomcore.facade;

import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HostOpsFacade {

    public Flux<Accommodation> findAccommodationsByHostId(String hostId, AccommodationSearchFilter filter) {
        return null;
    }

    public Flux<Booking> findAccommodationBookingsByFilter(String accommodationId, String hostId, BookingSearchFilter filter) {
        return null;
    }

    public Mono<Accommodation> updateAccommodation(String accommodationId, String hostId, UpdateAccommodation update) {
        return null;
    }

    public Mono<Booking> confirmBooking(String accommodationId, String bookingId, String hostId) {
        return null;
    }

    public Mono<Booking> cancelBooking(String accommodationId, String bookingId, String hostId) {
        return null;
    }

    public Mono<Accommodation> unlistAccommodation(String accommodationId, String hostId) {
        return null;
    }
}

package com.findaroom.findaroomcore.facade;

import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.dto.ReviewAccommodation;
import com.findaroom.findaroomcore.dto.aggregates.BookingDetails;
import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.dto.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserOpsFacade {

    public Flux<Booking> findBookingsByUserId(String userId, BookingSearchFilter filter) {
        return null;
    }

    public Flux<Review> findReviewsByUserId(String userId, ReviewSearchFilter filter) {
        return null;
    }

    public Mono<BookingDetails> findBookingDetails(String bookingId, String userId) {
        return null;
    }

    public Mono<Accommodation> saveAccommodation(String userId, Boolean superHost, CreateAccommodation create) {
        return null;
    }

    public Mono<BookingDetails> bookAccommodation(String accommodationId, String userId, BookAccommodation book) {
        return null;
    }

    public Mono<Review> reviewAccommodation(String accommodationId, String bookingId, String userId, ReviewAccommodation review) {
        return null;
    }

    public Mono<Booking> cancelBooking(String bookingId, String userId) {
        return null;
    }

    public Mono<Booking> rescheduleBooking(String bookingId, String userId, BookingDates reschedule) {
        return null;
    }
}

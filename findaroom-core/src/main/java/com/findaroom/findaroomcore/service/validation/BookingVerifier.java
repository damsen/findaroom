package com.findaroom.findaroomcore.service.validation;

import com.findaroom.findaroomcore.controller.event.BookingDates;
import com.findaroom.findaroomcore.domain.Booking;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.findaroom.findaroomcore.utils.MessageUtils.*;

@Component
public class BookingVerifier implements BusinessVerifier {

    public Mono<Booking> verifyBookingIsCompleted(Booking object) {
        return verify(
                object,
                Booking::isCompleted,
                BOOKING_NOT_COMPLETED
        );
    }

    public Mono<Booking> verifyBookingIsActive(Booking object) {
        return verify(
                object,
                Booking::isActive,
                BOOKING_NOT_ACTIVE
        );
    }

    public Mono<Booking> verifyBookingIsPending(Booking object) {
        return verify(
                object,
                Booking::isPending,
                BOOKING_NOT_PENDING
        );
    }

    public Mono<Booking> verifyBookingHasDifferentDatesThan(Booking object, BookingDates reschedule) {
        return verify(
                object,
                booking -> booking.hasDifferentDatesThan(reschedule),
                BOOKING_DATES_SAME_AS_RESCHEDULE_DATES
        );
    }

}

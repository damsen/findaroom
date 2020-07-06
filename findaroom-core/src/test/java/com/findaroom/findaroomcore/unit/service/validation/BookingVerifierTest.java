package com.findaroom.findaroomcore.unit.service.validation;

import com.findaroom.findaroomcore.controller.event.BookingDates;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
import com.findaroom.findaroomcore.service.validation.BookingVerifier;
import com.findaroom.findaroomcore.utils.TestPojos;
import com.findaroom.findaroomcore.utils.TestPredicates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static com.findaroom.findaroomcore.utils.MessageUtils.*;

@ExtendWith(SpringExtension.class)
public class BookingVerifierTest {

    @InjectMocks
    private BookingVerifier verifier;

    @Test
    public void verifyBookingIsCompleted() {

        Booking book = TestPojos.booking();
        book.setStatus(BookingStatus.DONE);
        book.setCheckout(LocalDate.now().minusDays(7));
        Mono<Booking> verified = verifier.verifyBookingIsCompleted(book);

        StepVerifier
                .create(verified)
                .expectNext(book)
                .verifyComplete();
    }

    @Test
    public void verifyBookingIsCompleted_whenBookingIsNotCompleted_shouldReturnUnprocessableEntity() {

        Mono<Booking> verified = verifier.verifyBookingIsCompleted(TestPojos.booking());

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_COMPLETED))
                .verify();
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"PENDING", "CONFIRMED"})
    public void verifyBookingIsActive(BookingStatus status) {

        Booking book = TestPojos.booking();
        book.setStatus(status);
        Mono<Booking> verified = verifier.verifyBookingIsActive(book);

        StepVerifier
                .create(verified)
                .expectNext(book)
                .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CANCELLED", "DONE"})
    public void verifyBookingIsActive_whenBookingIsNotActive_shouldReturnUnprocessableEntity(BookingStatus status) {

        Booking book = TestPojos.booking();
        book.setStatus(status);
        Mono<Booking> verified = verifier.verifyBookingIsActive(book);

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_ACTIVE))
                .verify();
    }

    @Test
    public void verifyBookingIsPending() {

        Booking book = TestPojos.booking();
        Mono<Booking> verified = verifier.verifyBookingIsPending(book);

        StepVerifier
                .create(verified)
                .expectNext(book)
                .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CONFIRMED", "CANCELLED", "DONE"})
    public void verifyBookingIsPending_whenBookingIsNotPending_shouldReturnUnprocessableEntity(BookingStatus status) {

        Booking book = TestPojos.booking();
        book.setStatus(status);
        Mono<Booking> verified = verifier.verifyBookingIsPending(book);

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_PENDING))
                .verify();
    }

    @Test
    public void verifyBookingHasDifferentDatesThan() {

        Booking book = TestPojos.booking();
        Mono<Booking> verified = verifier.verifyBookingHasDifferentDatesThan(book, TestPojos.bookingDates());

        StepVerifier
                .create(verified)
                .expectNext(book)
                .verifyComplete();
    }

    @Test
    public void verifyBookingHasDifferentDatesThan_whenRescheduleHasSameDatesAsBooking_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        BookingDates reschedule = new BookingDates(book.getCheckin(), book.getCheckout());
        Mono<Booking> verified = verifier.verifyBookingHasDifferentDatesThan(book, reschedule);

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_DATES_SAME_AS_RESCHEDULE_DATES))
                .verify();
    }
}

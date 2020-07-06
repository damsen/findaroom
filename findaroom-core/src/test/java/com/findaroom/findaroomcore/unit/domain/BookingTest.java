package com.findaroom.findaroomcore.unit.domain;

import com.findaroom.findaroomcore.controller.event.BookAccommodation;
import com.findaroom.findaroomcore.controller.event.BookingDates;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.TestPojos.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingTest {

    @Test
    public void fromBook_shouldReturnBookingWithMatchingProperties() {

        BookAccommodation book = bookAccommodation();
        Booking booking = Booking.from("123", "444", book);

        assertThat(booking.getAccommodationId()).isEqualTo("123");
        assertThat(booking.getUserId()).isEqualTo("444");
        assertThat(booking.getCheckin()).isEqualTo(book.getBookingDates().getCheckin());
        assertThat(booking.getCheckout()).isEqualTo(book.getBookingDates().getCheckout());
        assertThat(booking.getGuests()).isEqualTo(book.getGuests());
    }

    @Test
    public void rescheduleWith_shouldReturnBookingWithUpdatedProperties() {

        BookingDates dates = bookingDates();
        Booking booking = booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking rescheduled = booking.rescheduleWith(dates);

        assertThat(rescheduled.getCheckin()).isEqualTo(bookingDates().getCheckin());
        assertThat(rescheduled.getCheckout()).isEqualTo(bookingDates().getCheckout());
        assertThat(rescheduled.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
}

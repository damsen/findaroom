package com.findaroom.findaroomcore.unit.model;

import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.PojoUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingTest {

    @Test
    public void fromBook_shouldReturnBookingWithMatchingProperties() {

        BookAccommodation book = bookAccommodation();
        Booking booking = Booking.from(book);

        assertThat(booking.getAccommodationId()).isEqualTo(book.getAccommodationId());
        assertThat(booking.getUserId()).isEqualTo(book.getUserId());
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

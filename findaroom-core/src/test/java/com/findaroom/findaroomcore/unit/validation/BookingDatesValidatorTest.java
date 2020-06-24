package com.findaroom.findaroomcore.unit.validation;

import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.validation.BookingDatesValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingDatesValidatorTest {

    BookingDatesValidator validator = new BookingDatesValidator();

    @Test
    public void bookingDatesValidator_whenDatesAreValid_shouldReturnTrue() {

        BookingDates bookingDates = new BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(8));
        boolean valid = validator.isValid(bookingDates, null);

        assertThat(valid).isTrue();
    }

    @Test
    public void bookingDatesValidator_whenCheckinIsToday_shouldReturnFalse() {

        BookingDates bookingDates = new BookingDates(LocalDate.now(), LocalDate.now().plusDays(2));
        boolean valid = validator.isValid(bookingDates, null);

        assertThat(valid).isFalse();
    }

    @Test
    public void bookingDatesValidator_whenCheckinCheckoutAreToday_shouldReturnFalse() {

        BookingDates bookingDates = new BookingDates(LocalDate.now(), LocalDate.now());
        boolean valid = validator.isValid(bookingDates, null);

        assertThat(valid).isFalse();
    }

    @Test
    public void bookingDatesValidator_whenCheckinCheckoutAreTheSameDay_shouldReturnFalse() {

        BookingDates bookingDates = new BookingDates(LocalDate.now().plusDays(14), LocalDate.now().plusDays(14));
        boolean valid = validator.isValid(bookingDates, null);

        assertThat(valid).isFalse();
    }

    @Test
    public void bookingDatesValidator_whenCheckoutBeforeCheckin_shouldReturnFalse() {

        BookingDates bookingDates = new BookingDates(LocalDate.now().plusDays(14), LocalDate.now().plusDays(3));
        boolean valid = validator.isValid(bookingDates, null);

        assertThat(valid).isFalse();
    }

}

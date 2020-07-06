package com.findaroom.findaroomcore.controller.validation;

import com.findaroom.findaroomcore.controller.event.BookingDates;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof BookingDates) {
            var dates = (BookingDates) value;
            var checkin = dates.getCheckin();
            var checkout = dates.getCheckout();
            return checkin.isBefore(checkout) && !LocalDate.now().equals(checkin) && !LocalDate.now().equals(checkout);
        }
        throw new IllegalArgumentException("Invalid target of ValidBookingDates annotation");
    }
}
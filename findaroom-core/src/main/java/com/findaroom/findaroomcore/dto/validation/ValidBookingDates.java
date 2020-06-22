package com.findaroom.findaroomcore.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = BookingDatesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidBookingDates {

    String message() default "Checkout must be after checkin";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.findaroom.findaroomcore.controller.event;

import com.findaroom.findaroomcore.controller.validation.BookingDatesValidator;
import com.findaroom.findaroomcore.controller.validation.ValidBookingDates;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.GroupSequence;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@GroupSequence({BookingDates.class, BookingDatesValidator.class})
@ValidBookingDates(groups = BookingDatesValidator.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDates {

    @NotNull @Future @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin;
    @NotNull @Future @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout;
}

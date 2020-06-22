package com.findaroom.findaroomcore.dto;

import com.findaroom.findaroomcore.dto.validation.BookingDatesValidator;
import com.findaroom.findaroomcore.dto.validation.ValidBookingDates;
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

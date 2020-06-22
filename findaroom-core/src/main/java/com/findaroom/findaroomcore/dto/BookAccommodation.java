package com.findaroom.findaroomcore.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAccommodation {

    @NotNull @Valid BookingDates bookingDates;
    @NotNull @Positive Integer guests;
    String accommodationId;
    String userId;
}

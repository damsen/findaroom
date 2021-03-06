package com.findaroom.findaroomcore.controller.event;

import com.findaroom.findaroomcore.domain.enums.AccommodationType;
import com.findaroom.findaroomcore.domain.enums.Amenity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAccommodation {

    @NotBlank String name;
    @NotBlank String description;
    @NotNull @Positive Double pricePerNight;
    @NotNull @Positive Integer maxGuests;
    @NotNull @PositiveOrZero Integer restrooms;
    @NotNull @PositiveOrZero Integer bedrooms;
    @NotNull @PositiveOrZero Integer beds;
    @NotNull AccommodationType type;
    @NotNull List<Amenity> amenities;
}
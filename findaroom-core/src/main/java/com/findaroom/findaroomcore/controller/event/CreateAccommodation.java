package com.findaroom.findaroomcore.controller.event;

import com.findaroom.findaroomcore.domain.enums.AccommodationType;
import com.findaroom.findaroomcore.domain.enums.Amenity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAccommodation {

    @NotBlank String name;
    @NotBlank String description;
    @NotNull @Positive Double pricePerNight;
    @NotNull @Positive Integer maxGuests;
    @NotNull @PositiveOrZero Integer restrooms;
    @NotNull @PositiveOrZero Integer bedrooms;
    @NotNull @PositiveOrZero Integer beds;
    @NotNull AccommodationType type;
    @NotNull @Valid Address address;
    @NotNull List<Amenity> amenities;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Address {

        @NotBlank String country;
        @NotBlank String city;
        @NotBlank @Size(min = 5, max = 5) String zipcode;
        @NotBlank String street;
        @NotNull @Valid Location location;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Location {

        @NotNull Double x;
        @NotNull Double y;

        public GeoJsonPoint toGeoJsonPoint(){
            return new GeoJsonPoint(x, y);
        }
    }
}

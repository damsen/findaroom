package com.findaroom.findaroomcore.dto.filter;

import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.model.enums.AccommodationType;
import com.findaroom.findaroomcore.model.enums.Amenity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Point;
import org.springframework.format.annotation.DateTimeFormat;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccommodationSearchFilter extends PagingAndSortingFilter {

    Double pricePerNight;
    Double rating;
    Integer maxGuests;
    String hostId;
    Boolean superHost;
    List<AccommodationType> type;
    String country;
    String city;
    List<Amenity> amenities;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate checkin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate checkout;
    List<String> exclude;
    Double ne_lat;
    Double ne_lng;
    Double sw_lat;
    Double sw_lng;

    public Mono<BookingDates> getBookingDates() {
        return Mono.zip(getCheckin(), getCheckout(), BookingDates::new);
    }

    public Mono<Point> getNeGeoPoint() {
        return Mono.zip(getNe_lng(), getNe_lat(), Point::new);
    }

    public Mono<Point> getSwGeoPoint() {
        return Mono.zip(getSw_lng(), getSw_lat(), Point::new);
    }

    public Mono<Box> getGeoBox() {
        return Mono.zip(getSwGeoPoint(), getNeGeoPoint(), Box::new);
    }

    public Mono<Double> getPricePerNight() {
        return Mono.justOrEmpty(pricePerNight);
    }

    public Mono<Double> getRating() {
        return Mono.justOrEmpty(rating);
    }

    public Mono<Integer> getMaxGuests() {
        return Mono.justOrEmpty(maxGuests);
    }

    public Mono<String> getHostId() {
        return Mono.justOrEmpty(hostId);
    }

    public Mono<Boolean> isSuperHost() {
        return Mono.justOrEmpty(superHost);
    }

    public Mono<List<AccommodationType>> getType() {
        return Mono.justOrEmpty(type);
    }

    public Mono<String> getCountry() {
        return Mono.justOrEmpty(country);
    }

    public Mono<String> getCity() {
        return Mono.justOrEmpty(city);
    }

    public Mono<List<Amenity>> getAmenities() {
        return Mono.justOrEmpty(amenities);
    }

    public Mono<LocalDate> getCheckin() {
        return Mono.justOrEmpty(checkin);
    }

    public Mono<LocalDate> getCheckout() {
        return Mono.justOrEmpty(checkout);
    }

    public Mono<List<String>> getExclude() {
        return Mono.justOrEmpty(exclude);
    }

    public Mono<Double> getNe_lat() {
        return Mono.justOrEmpty(ne_lat);
    }

    public Mono<Double> getNe_lng() {
        return Mono.justOrEmpty(ne_lng);
    }

    public Mono<Double> getSw_lat() {
        return Mono.justOrEmpty(sw_lat);
    }

    public Mono<Double> getSw_lng() {
        return Mono.justOrEmpty(sw_lng);
    }
}

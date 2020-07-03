package com.findaroom.findaroomcore.utils;

import com.findaroom.findaroomcore.dto.*;
import com.findaroom.findaroomcore.model.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.findaroom.findaroomcore.model.enums.AccommodationType.WHOLE_APARTMENT;
import static com.findaroom.findaroomcore.model.enums.AccommodationType.WHOLE_LOFT;
import static com.findaroom.findaroomcore.model.enums.Amenity.*;

public class TestPojos {

    public static Accommodation accommodation() {
        return Accommodation.of("name", "desc", 100.0, 4, 1, 1, 2, host(), WHOLE_APARTMENT, address(), List.of(WIFI, KITCHEN));
    }

    public static Address address() {
        return new Address("country", "city", "12121", "street", new GeoJsonPoint(44.0, -1.0));
    }

    public static Host host() {
        return new Host("12345", false);
    }

    public static Booking booking() {
        return Booking.of("accommodationId", "userId", LocalDate.now().plusDays(7), LocalDate.now().plusDays(14), 3);
    }

    public static Review review() {
        return Review.of("accommodationId", "userId", UUID.randomUUID().toString(), 5.0, "message");
    }

    public static CreateAccommodation createAccommodation() {
        CreateAccommodation create = new CreateAccommodation();
        create.setName("name");
        create.setDescription("desc");
        create.setPricePerNight(100.0);
        create.setMaxGuests(4);
        create.setRestrooms(1);
        create.setBedrooms(1);
        create.setBeds(2);
        create.setType(WHOLE_APARTMENT);
        create.setAddress(createAddress());
        create.setAmenities(List.of(WIFI, KITCHEN));
        return create;
    }

    public static CreateAccommodation.Address createAddress() {
        CreateAccommodation.Address address = new CreateAccommodation.Address();
        address.setCountry("country");
        address.setCity("city");
        address.setZipcode("12345");
        address.setStreet("street");
        CreateAccommodation.Location location = new CreateAccommodation.Location();
        location.setX(44.003213);
        location.setY(-1.0002324);
        address.setLocation(location);
        return address;
    }

    public static UpdateAccommodation updateAccommodation() {
        UpdateAccommodation update = new UpdateAccommodation();
        update.setName("n");
        update.setDescription("d");
        update.setPricePerNight(20.0);
        update.setMaxGuests(1);
        update.setRestrooms(2);
        update.setBedrooms(2);
        update.setBeds(3);
        update.setType(WHOLE_LOFT);
        update.setAmenities(List.of(PARKING_SPOT));
        return update;
    }

    public static ReviewAccommodation reviewAccommodation() {
        ReviewAccommodation review = new ReviewAccommodation();
        review.setMessage("message");
        review.setRating(4.0);
        return review;
    }

    public static BookAccommodation bookAccommodation() {
        BookAccommodation book = new BookAccommodation();
        BookingDates bookingDates = new BookingDates();
        bookingDates.setCheckin(LocalDate.now().plusDays(7));
        bookingDates.setCheckout(LocalDate.now().plusDays(14));
        book.setBookingDates(bookingDates);
        book.setGuests(2);
        return book;
    }

    public static BookingDates bookingDates() {
        return new BookingDates(LocalDate.now().plusDays(30), LocalDate.now().plusDays(40));
    }
}

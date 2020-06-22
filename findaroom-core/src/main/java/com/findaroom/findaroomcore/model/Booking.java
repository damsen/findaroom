package com.findaroom.findaroomcore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.model.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.DONE;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.PENDING;

@Data
@Document(collection = "bookings")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    String bookingId;
    @Indexed
    String accommodationId;
    @Indexed
    String userId;
    @Indexed(direction = IndexDirection.DESCENDING)
    LocalDate checkin;
    @Indexed(direction = IndexDirection.DESCENDING)
    LocalDate checkout;
    int guests;
    Instant createTime;
    BookingStatus status;

    public static Booking of(String accommodationId, String userId, LocalDate checkin, LocalDate checkout, int guests) {
        return new Booking(null, accommodationId, userId, checkin, checkout, guests, Instant.now().truncatedTo(ChronoUnit.MILLIS), PENDING);
    }

    public static Booking from(BookAccommodation book) {
        return Booking.of(
                book.getAccommodationId(),
                book.getUserId(),
                book.getBookingDates().getCheckin(),
                book.getBookingDates().getCheckout(),
                book.getGuests()
        );
    }

    public Booking rescheduleWith(BookingDates bookingDates) {
        this.checkin = bookingDates.getCheckin();
        this.checkout = bookingDates.getCheckout();
        this.status = PENDING;
        return this;
    }

    @JsonIgnore
    public boolean isActive() {
        return LocalDate.now().isBefore(checkin) && BookingStatus.activeStates().contains(status);
    }

    @JsonIgnore
    public boolean isCompleted() {
        return LocalDate.now().isAfter(checkout) && Objects.equals(status, DONE);
    }

    @JsonIgnore
    public boolean isPending() {
        return Objects.equals(status, PENDING);
    }

    public boolean hasDifferentDatesThan(BookingDates bookingDates) {
        return !this.checkin.isEqual(bookingDates.getCheckin()) || !this.checkout.isEqual(bookingDates.getCheckout());
    }
}

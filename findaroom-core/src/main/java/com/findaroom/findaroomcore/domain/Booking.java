package com.findaroom.findaroomcore.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.findaroom.findaroomcore.controller.event.BookAccommodation;
import com.findaroom.findaroomcore.controller.event.BookingDates;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
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

import static com.findaroom.findaroomcore.domain.enums.BookingStatus.DONE;
import static com.findaroom.findaroomcore.domain.enums.BookingStatus.PENDING;

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

    public static Booking from(String accommodationId, String userId, BookAccommodation book) {
        return Booking.of(
                accommodationId,
                userId,
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
        return LocalDate.now().isBefore(this.checkin) && BookingStatus.activeStates().contains(this.status);
    }

    @JsonIgnore
    public boolean isCompleted() {
        return LocalDate.now().isAfter(this.checkout) && Objects.equals(this.status, DONE);
    }

    @JsonIgnore
    public boolean isPending() {
        return Objects.equals(this.status, PENDING);
    }

    public boolean hasDifferentDatesThan(BookingDates bookingDates) {
        return !this.checkin.isEqual(bookingDates.getCheckin()) || !this.checkout.isEqual(bookingDates.getCheckout());
    }
}

package com.findaroom.findaroomcore.dto.filter;

import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.model.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSearchFilter extends PagingAndSortingFilter {

    String accommodationId;
    String userId;
    List<BookingStatus> status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate checkin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate checkout;

    public BookingSearchFilter(BookingDates dates) {
        this.status = BookingStatus.activeStates();
        this.checkin = dates.getCheckin();
        this.checkout = dates.getCheckout();
    }

    public Mono<String> getAccommodationId() {
        return Mono.justOrEmpty(accommodationId);
    }

    public Mono<String> getUserId() {
        return Mono.justOrEmpty(userId);
    }

    public Mono<List<BookingStatus>> getStatus() {
        return Mono.justOrEmpty(status);
    }

    public Mono<LocalDate> getCheckin() {
        return Mono.justOrEmpty(checkin);
    }

    public Mono<LocalDate> getCheckout() {
        return Mono.justOrEmpty(checkout);
    }
}

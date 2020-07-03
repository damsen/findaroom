package com.findaroom.findaroomcore.service.verifier;

import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.repo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.activeStates;
import static com.findaroom.findaroomcore.utils.MessageUtils.*;

@Component
@RequiredArgsConstructor
public class AccommodationVerifier implements BusinessVerifier {

    private final BookingRepository bookingRepo;

    public Mono<Accommodation> verifyUserIsNotAccommodationHost(Accommodation object, String userId) {
        return verify(
                object,
                accommodation -> !accommodation.hasHostWithId(userId),
                USER_IS_ACCOMMODATION_HOST
        );
    }

    public Mono<Accommodation> verifyGuestsDoNotExceedCapacity(Accommodation object, int guests) {
        return verify(
                object,
                accommodation -> accommodation.fitsGuests(guests),
                ACCOMMODATION_MAX_GUESTS_EXCEEDED
        );
    }

    public Mono<Accommodation> verifyAccommodationIsListed(Accommodation object) {
        return verify(
                object,
                Accommodation::isListed,
                ACCOMMODATION_ALREADY_UNLISTED
        );
    }

    public Mono<Accommodation> verifyAccommodationIsAvailable(Accommodation object, BookingDates dates) {
        return verifyAsync(
                object,
                accommodation -> bookingRepo
                        .countActiveAccommodationBookingsBetweenDates(
                                accommodation.getAccommodationId(), dates.getCheckin(), dates.getCheckout(), activeStates())
                        .map(count -> count == 0),
                ACCOMMODATION_ALREADY_BOOKED
        );
    }

    public Mono<Accommodation> verifyAccommodationIsAvailableExcludingBooking(Accommodation object, String bookingId, BookingDates dates) {
        return verifyAsync(
                object,
                accommodation -> bookingRepo
                        .countActiveAccommodationBookingsBetweenDatesExcludingBooking(
                                accommodation.getAccommodationId(), bookingId, dates.getCheckin(), dates.getCheckout(), activeStates())
                        .map(count -> count == 0),
                ACCOMMODATION_ALREADY_BOOKED
        );
    }
}

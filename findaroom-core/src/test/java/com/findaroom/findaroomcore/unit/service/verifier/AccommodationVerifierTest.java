package com.findaroom.findaroomcore.unit.service.verifier;

import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.service.verifier.AccommodationVerifier;
import com.findaroom.findaroomcore.utils.TestPojos;
import com.findaroom.findaroomcore.utils.TestPredicates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.findaroom.findaroomcore.utils.MessageUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AccommodationVerifierTest {

    @Mock
    private BookingRepository bookingRepo;

    @InjectMocks
    private AccommodationVerifier verifier;

    @Test
    public void verifyUserIsNotAccommodationHost() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("123");
        Mono<Accommodation> verified = verifier.verifyUserIsNotAccommodationHost(acc, "444");

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyUserIsNotAccommodationHost_whenUserIsHost_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("444");
        Mono<Accommodation> verified = verifier.verifyUserIsNotAccommodationHost(acc, "444");

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(USER_IS_ACCOMMODATION_HOST))
                .verify();
    }

    @Test
    public void verifyGuestsDoNotExceedCapacity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setMaxGuests(4);
        Mono<Accommodation> verified = verifier.verifyGuestsDoNotExceedCapacity(acc, 2);

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyGuestsDoNotExceedCapacity_whenGuestsExceedCapacity_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setMaxGuests(1);
        Mono<Accommodation> verified = verifier.verifyGuestsDoNotExceedCapacity(acc, 3);

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_MAX_GUESTS_EXCEEDED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsListed() {

        Accommodation acc = TestPojos.accommodation();
        Mono<Accommodation> verified = verifier.verifyAccommodationIsListed(acc);

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsListed_whenAccommodationIsUnlisted_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setListed(false);
        Mono<Accommodation> verified = verifier.verifyAccommodationIsListed(acc);

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_UNLISTED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsAvailable() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(0L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailable(acc, TestPojos.bookingDates());

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsAvailable_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(1L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailable(acc, TestPojos.bookingDates());

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsAvailableExcludingBooking() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(0L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailableExcludingBooking(acc, "111", TestPojos.bookingDates());

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsAvailableExcludingBooking_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(1L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailableExcludingBooking(acc, "111", TestPojos.bookingDates());

        StepVerifier
                .create(verified)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }
}

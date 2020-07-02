package com.findaroom.findaroomcore.unit.service.verifier;

import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.service.verifier.AccommodationVerifier;
import com.findaroom.findaroomcore.utils.PojoUtils;
import com.findaroom.findaroomcore.utils.PredicateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.findaroom.findaroomcore.utils.MessageUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccommodationVerifierTest {

    private AccommodationVerifier verifier;

    @MockBean
    private BookingRepository bookingRepo;

    @BeforeAll
    public void setup() {
        verifier = new AccommodationVerifier(bookingRepo);
    }

    @Test
    public void verifyUserIsNotAccommodationHost() {

        Accommodation acc = PojoUtils.accommodation();
        acc.getHost().setHostId("123");
        Mono<Accommodation> verified = verifier.verifyUserIsNotAccommodationHost(acc, "444");

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyUserIsNotAccommodationHost_whenUserIsHost_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.getHost().setHostId("444");
        Mono<Accommodation> verified = verifier.verifyUserIsNotAccommodationHost(acc, "444");

        StepVerifier
                .create(verified)
                .expectErrorMatches(PredicateUtils.unprocessableEntity(USER_IS_ACCOMMODATION_HOST))
                .verify();
    }

    @Test
    public void verifyGuestsDoNotExceedCapacity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setMaxGuests(4);
        Mono<Accommodation> verified = verifier.verifyGuestsDoNotExceedCapacity(acc, 2);

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyGuestsDoNotExceedCapacity_whenGuestsExceedCapacity_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setMaxGuests(1);
        Mono<Accommodation> verified = verifier.verifyGuestsDoNotExceedCapacity(acc, 3);

        StepVerifier
                .create(verified)
                .expectErrorMatches(PredicateUtils.unprocessableEntity(ACCOMMODATION_MAX_GUESTS_EXCEEDED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsListed() {

        Accommodation acc = PojoUtils.accommodation();
        Mono<Accommodation> verified = verifier.verifyAccommodationIsListed(acc);

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsListed_whenAccommodationIsUnlisted_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setListed(false);
        Mono<Accommodation> verified = verifier.verifyAccommodationIsListed(acc);

        StepVerifier
                .create(verified)
                .expectErrorMatches(PredicateUtils.unprocessableEntity(ACCOMMODATION_ALREADY_UNLISTED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsAvailable() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(0L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailable(acc, PojoUtils.bookingDates());

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsAvailable_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(1L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailable(acc, PojoUtils.bookingDates());

        StepVerifier
                .create(verified)
                .expectErrorMatches(PredicateUtils.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }

    @Test
    public void verifyAccommodationIsAvailableExcludingBooking() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(0L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailableExcludingBooking(acc, "111", PojoUtils.bookingDates());

        StepVerifier
                .create(verified)
                .expectNext(acc)
                .verifyComplete();
    }

    @Test
    public void verifyAccommodationIsAvailableExcludingBooking_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), anyList()))
                .thenReturn(Mono.just(1L));

        Mono<Accommodation> verified = verifier.verifyAccommodationIsAvailableExcludingBooking(acc, "111", PojoUtils.bookingDates());

        StepVerifier
                .create(verified)
                .expectErrorMatches(PredicateUtils.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }
}

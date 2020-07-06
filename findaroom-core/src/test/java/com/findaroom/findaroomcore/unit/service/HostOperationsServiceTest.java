package com.findaroom.findaroomcore.unit.service;

import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.service.HostOperationsService;
import com.findaroom.findaroomcore.service.verifier.AccommodationVerifier;
import com.findaroom.findaroomcore.service.verifier.BookingVerifier;
import com.findaroom.findaroomcore.utils.ErrorUtils;
import com.findaroom.findaroomcore.utils.TestPojos;
import com.findaroom.findaroomcore.utils.TestPredicates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.CONFIRMED;
import static com.findaroom.findaroomcore.utils.MessageUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class HostOperationsServiceTest {

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private AccommodationVerifier accommodationVerifier;

    @Mock
    private BookingVerifier bookingVerifier;

    @InjectMocks
    private HostOperationsService hostOps;

    @Test
    public void findAccommodationsByHostId() {

        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.accommodation()));

        var filter = new AccommodationSearchFilter();
        Flux<Accommodation> accommodations = hostOps.findAccommodationsByHostId("444", filter);

        StepVerifier
                .create(accommodations)
                .expectNextCount(1)
                .verifyComplete();

        assertThat(filter.getHostId().block()).isEqualTo("444");
    }

    @Test
    public void findAccommodationBookingsByFilter() {

        Accommodation accommodation = TestPojos.accommodation();
        accommodation.setAccommodationId("123");
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(accommodation));
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.booking(), TestPojos.booking()));

        var filter = new BookingSearchFilter();
        Flux<Booking> bookings = hostOps.findAccommodationBookingsByFilter("123", "444", filter);

        StepVerifier
                .create(bookings)
                .expectNextCount(2)
                .verifyComplete();

        assertThat(filter.getAccommodationId().block()).isEqualTo("123");
    }

    @Test
    public void findAccommodationBookingsByFilter_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());

        Flux<Booking> bookings = hostOps.findAccommodationBookingsByFilter("123", "444", new BookingSearchFilter());

        StepVerifier
                .create(bookings)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void findAccommodationBookingsByFilter_whenBookingsNotFound_shouldReturnEmpty() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.empty());

        Flux<Booking> bookings = hostOps.findAccommodationBookingsByFilter("123", "444", new BookingSearchFilter());

        StepVerifier
                .create(bookings)
                .verifyComplete();
    }

    @Test
    public void updateAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationRepo.save(any())).thenReturn(Mono.just(acc));

        var update = TestPojos.updateAccommodation();
        update.setName("new");
        Mono<Accommodation> accommodation = hostOps.updateAccommodation("123", "444", update);

        StepVerifier
                .create(accommodation)
                .assertNext(a -> assertThat(a.getName()).isEqualTo("new"))
                .verifyComplete();
    }

    @Test
    public void updateAccommodation_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Accommodation> accommodation = hostOps.updateAccommodation("123", "444", TestPojos.updateAccommodation());

        StepVerifier
                .create(accommodation)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void confirmBooking() {

        Booking book = TestPojos.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsPending(any())).thenReturn(Mono.just(book));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b.getStatus()).isEqualTo(CONFIRMED))
                .verifyComplete();
    }

    @Test
    public void confirmBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        Booking book = TestPojos.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsPending(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void confirmBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void confirmBooking_whenBookingNotPending_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setStatus(CANCELLED);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsPending(any())).thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_NOT_PENDING)));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_PENDING))
                .verify();
    }

    @Test
    public void cancelBooking() {

        Booking book = TestPojos.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b.getStatus()).isEqualTo(CANCELLED))
                .verifyComplete();
    }

    @Test
    public void cancelBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        Booking book = TestPojos.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setStatus(CANCELLED);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_NOT_ACTIVE)));


        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_ACTIVE))
                .verify();
    }

    @Test
    public void unlistAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        Booking book1 = TestPojos.booking();
        Booking book2 = TestPojos.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsListed(any())).thenReturn(Mono.just(acc));
        when(accommodationRepo.save(any())).thenReturn(Mono.just(acc));
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(book1, book2));
        when(bookingRepo.saveAll(anyIterable())).thenReturn(Flux.just(book1, book2));

        Mono<Accommodation> unlisted = hostOps.unlistAccommodation("123", "444");

        StepVerifier
                .create(unlisted)
                .assertNext(a -> {
                    assertThat(a.isListed()).isFalse();
                    assertThat(book1.getStatus()).isEqualTo(CANCELLED);
                    assertThat(book2.getStatus()).isEqualTo(CANCELLED);
                })
                .verifyComplete();
    }

    @Test
    public void unlistAccommodation_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Accommodation> unlisted = hostOps.unlistAccommodation("123", "444");

        StepVerifier
                .create(unlisted)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void unlistAccommodation_whenAccommodationAlreadyUnlisted_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setListed(false);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsListed(any()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(ACCOMMODATION_ALREADY_UNLISTED)));

        Mono<Accommodation> unlisted = hostOps.unlistAccommodation("123", "444");

        StepVerifier
                .create(unlisted)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_UNLISTED))
                .verify();
    }
}

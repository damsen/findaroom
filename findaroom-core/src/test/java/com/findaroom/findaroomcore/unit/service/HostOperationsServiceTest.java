package com.findaroom.findaroomcore.unit.service;

import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.service.HostOperationsService;
import com.findaroom.findaroomcore.utils.PojoUtils;
import com.findaroom.findaroomcore.utils.PredicateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.CONFIRMED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HostOperationsServiceTest {

    @MockBean
    private AccommodationRepository accommodationRepo;

    @MockBean
    private BookingRepository bookingRepo;

    private HostOperationsService hostOps;

    @BeforeAll
    public void setup() {
        hostOps = new HostOperationsService(accommodationRepo, bookingRepo);
    }

    @Test
    public void findAccommodationsByHostId() {

        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.accommodation()));

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

        Accommodation accommodation = PojoUtils.accommodation();
        accommodation.setAccommodationId("123");
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(accommodation));
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.booking(), PojoUtils.booking()));

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
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void findAccommodationBookingsByFilter_whenBookingsNotFound_shouldReturnEmpty() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.empty());

        Flux<Booking> bookings = hostOps.findAccommodationBookingsByFilter("123", "444", new BookingSearchFilter());

        StepVerifier
                .create(bookings)
                .verifyComplete();
    }

    @Test
    public void updateAccommodation() {

        Accommodation acc = PojoUtils.accommodation();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationRepo.save(any())).thenReturn(Mono.just(acc));

        var update = PojoUtils.updateAccommodation();
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

        Mono<Accommodation> accommodation = hostOps.updateAccommodation("123", "444", PojoUtils.updateAccommodation());

        StepVerifier
                .create(accommodation)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void confirmBooking() {

        Booking book = PojoUtils.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b.getStatus()).isEqualTo(CONFIRMED))
                .verifyComplete();
    }

    @Test
    public void confirmBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void confirmBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void confirmBooking_whenBookingNotPending_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setStatus(CANCELLED);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.confirmBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void cancelBooking() {

        Booking book = PojoUtils.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b.getStatus()).isEqualTo(CANCELLED))
                .verifyComplete();
    }

    @Test
    public void cancelBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setStatus(CANCELLED);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationId(anyString(), anyString())).thenReturn(Mono.just(book));

        Mono<Booking> booking = hostOps.cancelBooking("123", "111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void unlistAccommodation() {

        Accommodation acc = PojoUtils.accommodation();
        Booking book1 = PojoUtils.booking();
        Booking book2 = PojoUtils.booking();
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));
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
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void unlistAccommodation_whenAccommodationAlreadyUnlisted_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setListed(false);
        when(accommodationRepo.findByAccommodationIdAndHostId(anyString(), anyString())).thenReturn(Mono.just(acc));

        Mono<Accommodation> unlisted = hostOps.unlistAccommodation("123", "444");

        StepVerifier
                .create(unlisted)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }
}

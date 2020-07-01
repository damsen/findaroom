package com.findaroom.findaroomcore.unit.service;

import com.findaroom.findaroomcore.dto.BookAccommodation;
import com.findaroom.findaroomcore.dto.BookingDates;
import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.dto.filters.ReviewSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.model.Review;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.service.UserOperationsService;
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

import java.time.LocalDate;
import java.util.List;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.DONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserOperationsServiceTest {

    @MockBean
    private AccommodationRepository accommodationRepo;

    @MockBean
    private BookingRepository bookingRepo;

    @MockBean
    private ReviewRepository reviewRepo;

    private UserOperationsService userOps;

    @BeforeAll
    public void setup() {
        userOps = new UserOperationsService(accommodationRepo, bookingRepo, reviewRepo);
    }

    @Test
    public void findBookingsByUserId() {

        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.booking(), PojoUtils.booking()));

        var filter = new BookingSearchFilter();
        Flux<Booking> bookings = userOps.findBookingsByUserId("444", filter);

        StepVerifier
                .create(bookings)
                .expectNextCount(2)
                .verifyComplete();

        assertThat(filter.getUserId().block()).isEqualTo("444");
    }

    @Test
    public void findReviewsByUserId() {

        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.review(), PojoUtils.review()));

        var filter = new ReviewSearchFilter();
        Flux<Review> reviews = userOps.findReviewsByUserId("444", filter);

        StepVerifier
                .create(reviews)
                .expectNextCount(2)
                .verifyComplete();

        assertThat(filter.getUserId().block()).isEqualTo("444");
    }

    @Test
    public void findUserFavorites() {

        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(PojoUtils.accommodation()));

        var filter = new AccommodationSearchFilter();
        Flux<Accommodation> favorites = userOps.findUserFavorites(List.of("123"), filter);

        StepVerifier
                .create(favorites)
                .expectNextCount(1)
                .verifyComplete();

        assertThat(filter.getSelect().block()).contains("123");
    }

    @Test
    public void findUserBookingById() {

        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        Mono<Booking> booking = userOps.findUserBookingById("111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b).isNotNull())
                .verifyComplete();
    }

    @Test
    public void findUserBookingById_whenBookingNotFound_shouldReturnNotFound() {

        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = userOps.findUserBookingById("111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void saveAccommodation() {

        when(accommodationRepo.save(any())).thenReturn(Mono.just(PojoUtils.accommodation()));

        Mono<Accommodation> accommodation = userOps.saveAccommodation("444", false, PojoUtils.createAccommodation());

        StepVerifier
                .create(accommodation)
                .assertNext(a -> assertThat(a).isNotNull())
                .verifyComplete();
    }

    @Test
    public void bookAccommodation() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.save(any())).thenReturn(Mono.just(PojoUtils.booking()));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", PojoUtils.bookAccommodation());

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b).isNotNull())
                .verifyComplete();
    }

    @Test
    public void bookAccommodation_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", PojoUtils.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void bookAccommodation_whenUserIsTheHost_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.getHost().setHostId("444");
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", PojoUtils.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void bookAccommodation_whenMaxGuestsExceeded_shouldReturnUnprocessableEntity() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setMaxGuests(2);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        BookAccommodation book = PojoUtils.bookAccommodation();
        book.setGuests(5);
        Mono<Booking> booking = userOps.bookAccommodation("123", "444", book);

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void bookAccommodation_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(1L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", PojoUtils.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void bookAccommodation_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(1L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", PojoUtils.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void reviewAccommodation() {

        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        Booking book = PojoUtils.booking();
        book.setCheckout(LocalDate.now().minusDays(6));
        book.setStatus(DONE);
        Review rev1 = PojoUtils.review();
        rev1.setRating(4.0);
        Review rev2 = PojoUtils.review();
        rev2.setRating(2.0);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(book));
        when(reviewRepo.save(any())).thenReturn(Mono.just(PojoUtils.review()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(rev1, rev2));
        when(accommodationRepo.save(any())).thenReturn(Mono.just(acc));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", PojoUtils.reviewAccommodation());

        StepVerifier
                .create(review)
                .assertNext(r -> {
                    assertThat(r).isNotNull();
                    assertThat(acc.getRating()).isEqualTo(3.0);
                })
                .verifyComplete();
    }

    @Test
    public void reviewAccommodation_whenAccommodationNotFound_shouldReturnNotFound() {

        Booking book = PojoUtils.booking();
        book.setCheckout(LocalDate.now().minusDays(6));
        book.setStatus(DONE);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(book));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", PojoUtils.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void reviewAccommodation_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", PojoUtils.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void reviewAccommodation_whenBookingNotCompleted_shouldReturnUnprocessableEntity() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(PojoUtils.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(PojoUtils.booking()));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", PojoUtils.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void cancelBooking() {

        Booking book = PojoUtils.booking();
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        Mono<Booking> booking = userOps.cancelBooking("111", "444");

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b.getStatus()).isEqualTo(CANCELLED))
                .verifyComplete();
    }

    @Test
    public void cancelBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Booking> booking = userOps.cancelBooking("111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setStatus(DONE);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));

        Mono<Booking> booking = userOps.cancelBooking("111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void rescheduleBooking() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        BookingDates reschedule = PojoUtils.bookingDates();
        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", reschedule);

        StepVerifier
                .create(booking)
                .assertNext(b -> {
                    assertThat(b.getCheckin()).isEqualTo(reschedule.getCheckin());
                    assertThat(b.getCheckout()).isEqualTo(reschedule.getCheckout());
                })
                .verifyComplete();
    }

    @Test
    public void rescheduleBooking_whenBookingNotFound_shouldReturnNotFound() {

        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.empty());
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", PojoUtils.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void rescheduleBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setStatus(CANCELLED);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", PojoUtils.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void rescheduleBooking_whenRescheduleHasSameDatesAsBooking_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        LocalDate checkin = LocalDate.now().plusDays(7);
        LocalDate checkout = LocalDate.now().plusDays(14);
        book.setCheckin(checkin);
        book.setCheckout(checkout);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        BookingDates reschedule = new BookingDates(checkin, checkout);
        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", reschedule);

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void rescheduleBooking_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(1L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", PojoUtils.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

    @Test
    public void rescheduleBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", PojoUtils.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.notFound())
                .verify();
    }

    @Test
    public void rescheduleBooking_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Booking book = PojoUtils.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = PojoUtils.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));
        when(bookingRepo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(1L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", PojoUtils.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(PredicateUtils.unprocessableEntity())
                .verify();
    }

}

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

import java.time.LocalDate;
import java.util.List;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.CANCELLED;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.DONE;
import static com.findaroom.findaroomcore.utils.MessageUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserOperationsServiceTest {

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private AccommodationVerifier accommodationVerifier;

    @Mock
    private BookingVerifier bookingVerifier;

    @InjectMocks
    private UserOperationsService userOps;

    @Test
    public void findBookingsByUserId() {

        when(bookingRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.booking(), TestPojos.booking()));

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

        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.review(), TestPojos.review()));

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

        when(accommodationRepo.findAllByFilter(any())).thenReturn(Flux.just(TestPojos.accommodation()));

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

        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(TestPojos.booking()));

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
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void saveAccommodation() {

        when(accommodationRepo.save(any())).thenReturn(Mono.just(TestPojos.accommodation()));

        Mono<Accommodation> accommodation = userOps.saveAccommodation("444", false, TestPojos.createAccommodation());

        StepVerifier
                .create(accommodation)
                .assertNext(a -> assertThat(a).isNotNull())
                .verifyComplete();
    }

    @Test
    public void bookAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyUserIsNotAccommodationHost(any(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyGuestsDoNotExceedCapacity(any(), anyInt())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailable(any(), any())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));
        when(bookingRepo.save(any())).thenReturn(Mono.just(TestPojos.booking()));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", TestPojos.bookAccommodation());

        StepVerifier
                .create(booking)
                .assertNext(b -> assertThat(b).isNotNull())
                .verifyComplete();
    }

    @Test
    public void bookAccommodation_whenAccommodationNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", TestPojos.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void bookAccommodation_whenUserIsTheHost_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("444");
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyUserIsNotAccommodationHost(any(), anyString()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(USER_IS_ACCOMMODATION_HOST)));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", TestPojos.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(USER_IS_ACCOMMODATION_HOST))
                .verify();
    }

    @Test
    public void bookAccommodation_whenMaxGuestsExceeded_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setMaxGuests(2);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyUserIsNotAccommodationHost(any(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyGuestsDoNotExceedCapacity(any(), anyInt()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(ACCOMMODATION_MAX_GUESTS_EXCEEDED)));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        BookAccommodation book = TestPojos.bookAccommodation();
        book.setGuests(5);
        Mono<Booking> booking = userOps.bookAccommodation("123", "444", book);

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_MAX_GUESTS_EXCEEDED))
                .verify();
    }

    @Test
    public void bookAccommodation_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyUserIsNotAccommodationHost(any(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyGuestsDoNotExceedCapacity(any(), anyInt())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailable(any(), any()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED)));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", TestPojos.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }

    @Test
    public void bookAccommodation_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyUserIsNotAccommodationHost(any(), anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyGuestsDoNotExceedCapacity(any(), anyInt())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailable(any(), any())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDates(anyString(), any(), any(), any())).thenReturn(Mono.just(1L));

        Mono<Booking> booking = userOps.bookAccommodation("123", "444", TestPojos.bookAccommodation());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(USER_HAS_BOOKINGS_BETWEEN_DATES))
                .verify();
    }

    @Test
    public void reviewAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        Booking book = TestPojos.booking();
        book.setCheckout(LocalDate.now().minusDays(6));
        book.setStatus(DONE);
        Review rev1 = TestPojos.review();
        rev1.setRating(4.0);
        Review rev2 = TestPojos.review();
        rev2.setRating(2.0);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsCompleted(any())).thenReturn(Mono.just(book));
        when(reviewRepo.save(any())).thenReturn(Mono.just(TestPojos.review()));
        when(reviewRepo.findAllByFilter(any())).thenReturn(Flux.just(rev1, rev2));
        when(accommodationRepo.save(any())).thenReturn(Mono.just(acc));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", TestPojos.reviewAccommodation());

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

        Booking book = TestPojos.booking();
        book.setCheckout(LocalDate.now().minusDays(6));
        book.setStatus(DONE);
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(book));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", TestPojos.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void reviewAccommodation_whenBookingNotFound_shouldReturnNotFound() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", TestPojos.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void reviewAccommodation_whenBookingNotCompleted_shouldReturnUnprocessableEntity() {

        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(TestPojos.accommodation()));
        when(bookingRepo.findByBookingIdAndAccommodationIdAndUserId(anyString(), anyString(), anyString())).thenReturn(Mono.just(TestPojos.booking()));
        when(bookingVerifier.verifyBookingIsCompleted(any())).thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_NOT_COMPLETED)));

        Mono<Review> review = userOps.reviewAccommodation("123", "111", "444", TestPojos.reviewAccommodation());

        StepVerifier
                .create(review)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_COMPLETED))
                .verify();
    }

    @Test
    public void cancelBooking() {

        Booking book = TestPojos.booking();
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
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
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setStatus(DONE);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_NOT_ACTIVE)));

        Mono<Booking> booking = userOps.cancelBooking("111", "444");

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_ACTIVE))
                .verify();
    }

    @Test
    public void rescheduleBooking() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingHasDifferentDatesThan(any(), any())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailableExcludingBooking(any(), anyString(), any())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));
        when(bookingRepo.save(any())).thenReturn(Mono.just(book));

        BookingDates reschedule = TestPojos.bookingDates();
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

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", TestPojos.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(BOOKING_NOT_FOUND))
                .verify();
    }

    @Test
    public void rescheduleBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setStatus(CANCELLED);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_NOT_ACTIVE)));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", TestPojos.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_NOT_ACTIVE))
                .verify();
    }

    @Test
    public void rescheduleBooking_whenRescheduleHasSameDatesAsBooking_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        LocalDate checkin = LocalDate.now().plusDays(7);
        LocalDate checkout = LocalDate.now().plusDays(14);
        book.setCheckin(checkin);
        book.setCheckout(checkout);
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingHasDifferentDatesThan(any(), any()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(BOOKING_DATES_SAME_AS_RESCHEDULE_DATES)));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        BookingDates reschedule = new BookingDates(checkin, checkout);
        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", reschedule);

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(BOOKING_DATES_SAME_AS_RESCHEDULE_DATES))
                .verify();
    }

    @Test
    public void rescheduleBooking_whenAccommodationNotFound_shouldReturnNotFound() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingHasDifferentDatesThan(any(), any())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.empty());
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", TestPojos.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.notFound(ACCOMMODATION_NOT_FOUND))
                .verify();
    }

    @Test
    public void rescheduleBooking_whenAccommodationIsBooked_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingHasDifferentDatesThan(any(), any())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailableExcludingBooking(any(), anyString(), any()))
                .thenReturn(Mono.error(ErrorUtils.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED)));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(0L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", TestPojos.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(ACCOMMODATION_ALREADY_BOOKED))
                .verify();
    }

    @Test
    public void rescheduleBooking_whenUserAlreadyHasBookingsBetweenDates_shouldReturnUnprocessableEntity() {

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        when(bookingRepo.findByBookingIdAndUserId(anyString(), anyString())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingIsActive(any())).thenReturn(Mono.just(book));
        when(bookingVerifier.verifyBookingHasDifferentDatesThan(any(), any())).thenReturn(Mono.just(book));
        when(accommodationRepo.findById(anyString())).thenReturn(Mono.just(acc));
        when(accommodationVerifier.verifyAccommodationIsAvailableExcludingBooking(any(), anyString(), any())).thenReturn(Mono.just(acc));
        when(bookingRepo.countActiveUserBookingsBetweenDatesExcludingBooking(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Mono.just(1L));

        Mono<Booking> booking = userOps.rescheduleBooking("111", "444", TestPojos.bookingDates());

        StepVerifier
                .create(booking)
                .expectErrorMatches(TestPredicates.unprocessableEntity(USER_HAS_BOOKINGS_BETWEEN_DATES))
                .verify();
    }

}

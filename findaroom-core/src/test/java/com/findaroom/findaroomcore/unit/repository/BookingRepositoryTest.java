package com.findaroom.findaroomcore.unit.repository;

import com.findaroom.findaroomcore.controller.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
import com.findaroom.findaroomcore.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.findaroom.findaroomcore.domain.enums.BookingStatus.*;
import static com.findaroom.findaroomcore.utils.TestPojos.booking;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository repo;

    @BeforeEach
    public void setup() {
        repo.deleteAll().block();
    }

    @Test
    public void findAllByFilter() {

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(booking(), booking()))
                .thenMany(repo.findAllByFilter(new BookingSearchFilter()));

        StepVerifier
                .create(bookings)
                .assertNext(b -> assertThat(b.getBookingId()).isNotNull())
                .assertNext(b -> assertThat(b.getBookingId()).isNotNull())
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withAccommodationIdFilter_shouldReturnFilteredResults() {

        Booking book1 = booking();
        book1.setAccommodationId("123");
        Booking book2 = booking();
        book2.setAccommodationId("456");

        var filter = new BookingSearchFilter();
        filter.setAccommodationId("123");

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(book1, book2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(bookings)
                .assertNext(b -> assertThat(b.getAccommodationId()).isEqualTo("123"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withUserIdFilter_shouldReturnFilteredResults() {

        Booking book1 = booking();
        book1.setUserId("123");
        Booking book2 = booking();
        book2.setUserId("456");

        var filter = new BookingSearchFilter();
        filter.setUserId("123");

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(book1, book2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(bookings)
                .assertNext(b -> assertThat(b.getUserId()).isEqualTo("123"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withStatusFilter_shouldReturnFilteredResults() {

        Booking book1 = booking();
        book1.setStatus(PENDING);
        Booking book2 = booking();
        book2.setStatus(DONE);
        Booking book3 = booking();
        book3.setStatus(CONFIRMED);

        var filter = new BookingSearchFilter();
        filter.setStatus(List.of(PENDING, CONFIRMED));

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(book1, book2, book3))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(bookings)
                .assertNext(b -> assertThat(b.getStatus()).isIn(PENDING, CONFIRMED))
                .assertNext(b -> assertThat(b.getStatus()).isIn(PENDING, CONFIRMED))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withBetweenFilter_shouldReturnFilteredResults() {

        Booking book1 = booking();
        book1.setBookingId("1");
        book1.setCheckin(LocalDate.of(2020, Month.MAY, 23));
        book1.setCheckout(LocalDate.of(2020, Month.MAY, 29));
        Booking book2 = booking();
        book2.setBookingId("2");
        book2.setCheckin(LocalDate.of(2020, Month.JUNE, 1));
        book2.setCheckout(LocalDate.of(2020, Month.JUNE, 4));
        Booking book3 = booking();
        book3.setBookingId("3");
        book3.setCheckin(LocalDate.of(2020, Month.MAY, 20));
        book3.setCheckout(LocalDate.of(2020, Month.MAY, 26));

        var filter = new BookingSearchFilter();
        filter.setCheckin(LocalDate.of(2020, Month.MAY, 19));
        filter.setCheckout(LocalDate.of(2020, Month.MAY, 25));

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(book1, book2, book3))
                .thenMany(repo.findAllByFilter(filter));

        Predicate<Booking> matches = b -> Objects.equals(b.getBookingId(), "1") ||
                                          Objects.equals(b.getBookingId(), "3");

        StepVerifier
                .create(bookings)
                .expectNextMatches(matches)
                .expectNextMatches(matches)
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withPaging_shouldReturnPagedResults() {

        var filter = new BookingSearchFilter();
        filter.setPage(0);
        filter.setSize(2);

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(booking(), booking(), booking()))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(bookings)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withSorting_shouldReturnSortedResults() {

        Booking book1 = booking();
        book1.setBookingId("1");
        book1.setStatus(PENDING);
        Booking book2 = booking();
        book2.setBookingId("2");
        book2.setStatus(DONE);
        Booking book3 = booking();
        book3.setBookingId("3");
        book3.setStatus(CANCELLED);
        book3.setCreateTime(Instant.now().plusSeconds(5));
        Booking book4 = booking();
        book4.setBookingId("4");
        book4.setStatus(CANCELLED);
        book4.setCreateTime(Instant.now().plusSeconds(10));

        var filter = new BookingSearchFilter();
        filter.setSortBy(List.of("status", "createTime"));
        filter.setDirection("ASC");

        Flux<Booking> bookings = repo
                .saveAll(Flux.just(book1, book2, book3, book4))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(bookings)
                .assertNext(b -> assertThat(b.getBookingId()).isEqualTo("3"))
                .assertNext(b -> assertThat(b.getBookingId()).isEqualTo("4"))
                .assertNext(b -> assertThat(b.getBookingId()).isEqualTo("2"))
                .assertNext(b -> assertThat(b.getBookingId()).isEqualTo("1"))
                .verifyComplete();
    }

    @Test
    public void countActiveAccommodationBookingsBetweenDates() {

        Booking book1 = booking();
        book1.setAccommodationId("123");
        book1.setCheckin(LocalDate.of(2020, Month.OCTOBER, 4));
        book1.setCheckout(LocalDate.of(2020, Month.OCTOBER, 7));
        Booking book2 = booking();
        book2.setAccommodationId("123");
        book2.setCheckin(LocalDate.of(2020, Month.OCTOBER, 8));
        book2.setCheckout(LocalDate.of(2020, Month.OCTOBER, 18));
        book2.setStatus(DONE);
        Booking book3 = booking();
        book3.setAccommodationId("123");
        book3.setCheckin(LocalDate.of(2020, Month.JULY, 10));
        book3.setCheckout(LocalDate.of(2020, Month.JULY, 11));
        Booking book4 = booking();
        book4.setAccommodationId("456");
        book4.setCheckin(LocalDate.of(2020, Month.SEPTEMBER, 20));
        book4.setCheckout(LocalDate.of(2020, Month.SEPTEMBER, 21));

        Mono<Long> count = repo.saveAll(Flux.just(book1, book2, book3, book4))
                .then(repo.countActiveAccommodationBookingsBetweenDates(
                        "123",
                        LocalDate.of(2020, Month.OCTOBER, 5),
                        LocalDate.of(2020, Month.OCTOBER, 15),
                        BookingStatus.activeStates()));

        StepVerifier
                .create(count)
                .assertNext(cnt -> assertThat(cnt).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    public void countActiveUserBookingsBetweenDates() {

        Booking book1 = booking();
        book1.setUserId("123");
        book1.setCheckin(LocalDate.of(2020, Month.OCTOBER, 4));
        book1.setCheckout(LocalDate.of(2020, Month.OCTOBER, 7));
        Booking book2 = booking();
        book2.setUserId("123");
        book2.setCheckin(LocalDate.of(2020, Month.OCTOBER, 8));
        book2.setCheckout(LocalDate.of(2020, Month.OCTOBER, 18));
        book2.setStatus(DONE);
        Booking book3 = booking();
        book3.setUserId("123");
        book3.setCheckin(LocalDate.of(2020, Month.JULY, 10));
        book3.setCheckout(LocalDate.of(2020, Month.JULY, 11));
        Booking book4 = booking();
        book4.setUserId("456");
        book4.setCheckin(LocalDate.of(2020, Month.SEPTEMBER, 20));
        book4.setCheckout(LocalDate.of(2020, Month.SEPTEMBER, 21));

        Mono<Long> count = repo.saveAll(Flux.just(book1, book2, book3, book4))
                .then(repo.countActiveUserBookingsBetweenDates(
                        "123",
                        LocalDate.of(2020, Month.OCTOBER, 5),
                        LocalDate.of(2020, Month.OCTOBER, 15),
                        BookingStatus.activeStates()));

        StepVerifier
                .create(count)
                .assertNext(cnt -> assertThat(cnt).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    public void countActiveAccommodationBookingsBetweenDatesExcludingBooking() {

        Booking book1 = booking();
        book1.setBookingId("111");
        book1.setAccommodationId("123");
        book1.setCheckin(LocalDate.of(2020, Month.OCTOBER, 4));
        book1.setCheckout(LocalDate.of(2020, Month.OCTOBER, 7));
        Booking book2 = booking();
        book2.setAccommodationId("123");
        book2.setCheckin(LocalDate.of(2020, Month.OCTOBER, 8));
        book2.setCheckout(LocalDate.of(2020, Month.OCTOBER, 18));
        Booking book3 = booking();
        book3.setAccommodationId("123");
        book3.setCheckin(LocalDate.of(2020, Month.JULY, 10));
        book3.setCheckout(LocalDate.of(2020, Month.JULY, 11));
        Booking book4 = booking();
        book4.setAccommodationId("456");
        book4.setCheckin(LocalDate.of(2020, Month.SEPTEMBER, 20));
        book4.setCheckout(LocalDate.of(2020, Month.SEPTEMBER, 21));

        Mono<Long> count = repo.saveAll(Flux.just(book1, book2, book3, book4))
                .then(repo.countActiveAccommodationBookingsBetweenDatesExcludingBooking(
                        "123",
                        "111",
                        LocalDate.of(2020, Month.OCTOBER, 5),
                        LocalDate.of(2020, Month.OCTOBER, 15),
                        BookingStatus.activeStates()));

        StepVerifier
                .create(count)
                .assertNext(cnt -> assertThat(cnt).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    public void countActiveUserBookingsBetweenDatesExcludingBooking() {

        Booking book1 = booking();
        book1.setBookingId("111");
        book1.setUserId("123");
        book1.setCheckin(LocalDate.of(2020, Month.OCTOBER, 4));
        book1.setCheckout(LocalDate.of(2020, Month.OCTOBER, 7));
        Booking book2 = booking();
        book2.setUserId("123");
        book2.setCheckin(LocalDate.of(2020, Month.OCTOBER, 8));
        book2.setCheckout(LocalDate.of(2020, Month.OCTOBER, 18));
        Booking book3 = booking();
        book3.setUserId("123");
        book3.setCheckin(LocalDate.of(2020, Month.JULY, 10));
        book3.setCheckout(LocalDate.of(2020, Month.JULY, 11));
        Booking book4 = booking();
        book4.setUserId("456");
        book4.setCheckin(LocalDate.of(2020, Month.SEPTEMBER, 20));
        book4.setCheckout(LocalDate.of(2020, Month.SEPTEMBER, 21));

        Mono<Long> count = repo.saveAll(Flux.just(book1, book2, book3, book4))
                .then(repo.countActiveUserBookingsBetweenDatesExcludingBooking(
                        "123",
                        "111",
                        LocalDate.of(2020, Month.OCTOBER, 5),
                        LocalDate.of(2020, Month.OCTOBER, 15),
                        BookingStatus.activeStates()));

        StepVerifier
                .create(count)
                .assertNext(cnt -> assertThat(cnt).isEqualTo(1))
                .verifyComplete();
    }
}

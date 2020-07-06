package com.findaroom.findaroomcore.repository;

import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.enums.BookingStatus;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String>, CustomBookingRepository {

    Mono<Booking> findByBookingIdAndAccommodationId(String bookingId, String accommodationId);

    Mono<Booking> findByBookingIdAndAccommodationIdAndUserId(String bookingId, String accommodationId, String userId);

    Mono<Booking> findByBookingIdAndUserId(String bookingId, String userId);

    @Query(value = "{accommodationId:?0,checkout:{$gte:?1},checkin:{$lte:?2},status:{$in:?3}}", count = true)
    Mono<Long> countActiveAccommodationBookingsBetweenDates(String accommodationId, LocalDate checkin, LocalDate checkout, List<BookingStatus> status);

    @Query(value = "{userId:?0,checkout:{$gte:?1},checkin:{$lte:?2},status:{$in:?3}}", count = true)
    Mono<Long> countActiveUserBookingsBetweenDates(String userId, LocalDate checkin, LocalDate checkout, List<BookingStatus> status);

    @Query(value = "{accommodationId:?0,bookingId:{$ne:?1},checkout:{$gte:?2},checkin:{$lte:?3},status:{$in:?4}}", count = true)
    Mono<Long> countActiveAccommodationBookingsBetweenDatesExcludingBooking(String accommodationId, String bookingId, LocalDate checkin, LocalDate checkout, List<BookingStatus> status);

    @Query(value = "{userId:?0,bookingId:{$ne:?1},checkout:{$gte:?2},checkin:{$lte:?3},status:{$in:?4}}", count = true)
    Mono<Long> countActiveUserBookingsBetweenDatesExcludingBooking(String userId, String bookingId, LocalDate checkin, LocalDate checkout, List<BookingStatus> status);

}

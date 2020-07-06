package com.findaroom.findaroomcore.repository;

import com.findaroom.findaroomcore.controller.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.domain.Booking;
import reactor.core.publisher.Flux;

public interface CustomBookingRepository {

    Flux<Booking> findAllByFilter(BookingSearchFilter filter);
}

package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filters.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Booking;
import reactor.core.publisher.Flux;

public interface CustomBookingRepository {

    Flux<Booking> findAllByFilter(BookingSearchFilter filter);
}

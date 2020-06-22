package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Booking;
import reactor.core.publisher.Flux;

public interface CustomBookingRepo {

    Flux<Booking> findAllByFilter(BookingSearchFilter filter);
}

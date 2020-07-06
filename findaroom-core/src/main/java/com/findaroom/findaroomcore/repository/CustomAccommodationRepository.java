package com.findaroom.findaroomcore.repository;

import com.findaroom.findaroomcore.controller.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.domain.Accommodation;
import reactor.core.publisher.Flux;

public interface CustomAccommodationRepository {

    Flux<Accommodation> findAllByFilter(AccommodationSearchFilter filter);
}

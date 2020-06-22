package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import reactor.core.publisher.Flux;

public interface CustomAccommodationRepo {

    Flux<Accommodation> findAllByFilter(AccommodationSearchFilter filter);
}

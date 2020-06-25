package com.findaroom.findaroomcore.repo;

import com.findaroom.findaroomcore.dto.filters.AccommodationSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import reactor.core.publisher.Flux;

public interface CustomAccommodationRepository {

    Flux<Accommodation> findAllByFilter(AccommodationSearchFilter filter);
}

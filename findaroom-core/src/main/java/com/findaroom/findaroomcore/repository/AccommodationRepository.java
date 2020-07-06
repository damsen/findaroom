package com.findaroom.findaroomcore.repository;

import com.findaroom.findaroomcore.domain.Accommodation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccommodationRepository extends ReactiveMongoRepository<Accommodation, String>, CustomAccommodationRepository {

    @Query("{'accommodationId':?0,'host.hostId':?1}")
    Mono<Accommodation> findByAccommodationIdAndHostId(String accommodationId, String hostId);

}

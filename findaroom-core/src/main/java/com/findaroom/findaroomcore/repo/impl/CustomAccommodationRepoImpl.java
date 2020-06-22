package com.findaroom.findaroomcore.repo.impl;

import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.repo.CustomAccommodationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
public class CustomAccommodationRepoImpl implements CustomAccommodationRepo {

    private final ReactiveMongoOperations mongoOps;

    @Override
    public Flux<Accommodation> findAllByFilter(AccommodationSearchFilter filter) {

        Query query = query(where("listed").is(true));

        return toCriteria(filter)
                .map(query::addCriteria)
                .then(filter.getPageable())
                .map(query::with)
                .flatMapMany(q -> mongoOps.find(q, Accommodation.class));
    }

    private Flux<CriteriaDefinition> toCriteria(AccommodationSearchFilter filter) {

        var priceFilter = filter.getPricePerNight().map(where("pricePerNight")::lte);
        var ratingFilter = filter.getRating().map(where("rating")::gte);
        var guestsFilter = filter.getMaxGuests().map(where("maxGuests")::gte);
        var hostIdFilter = filter.getHostId().map(where("host.hostId")::is);
        var superHostFilter = filter.isSuperHost().map(where("host.superHost")::is);
        var typeFilter = filter.getType().map(where("type")::in);
        var countryFilter = filter.getCountry().map(where("address.country")::is);
        var cityFilter = filter.getCity().map(where("address.city")::is);
        var amenitiesFilter = filter.getAmenities().map(where("amenities")::all);
        var excludeFilter = filter.getExclude().map(where("accommodationId")::nin);
        var boxFilter = filter.getGeoBox().map(where("address.location")::within);

        return Flux.merge(priceFilter, ratingFilter, guestsFilter, hostIdFilter, superHostFilter, typeFilter,
                countryFilter, cityFilter, amenitiesFilter, excludeFilter, boxFilter);
    }

}

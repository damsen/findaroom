package com.findaroom.findaroomcore.unit.repo;

import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.repo.AccommodationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

import static com.findaroom.findaroomcore.model.enums.AccommodationType.*;
import static com.findaroom.findaroomcore.model.enums.Amenity.*;
import static com.findaroom.findaroomcore.utils.PojoUtils.accommodation;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class AccommodationRepoTest {

    @Autowired
    private AccommodationRepo repo;

    @BeforeEach
    public void setup() {
        repo.deleteAll().block();
    }

    @Test
    public void findAllByFilter_shouldReturnListedResults() {

        Accommodation unlisted = accommodation();
        unlisted.setListed(false);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(accommodation(), accommodation(), unlisted))
                .thenMany(repo.findAllByFilter(new AccommodationSearchFilter()));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.isListed()).isTrue())
                .assertNext(a -> assertThat(a.isListed()).isTrue())
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withPricePerNightFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setPricePerNight(120.0);
        Accommodation acc2 = accommodation();
        acc2.setPricePerNight(80.0);

        var filter = new AccommodationSearchFilter();
        filter.setPricePerNight(100.0);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getPricePerNight()).isLessThanOrEqualTo(100.0))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withRatingFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setRating(3.0);
        Accommodation acc2 = accommodation();
        acc2.setRating(5.0);

        var filter = new AccommodationSearchFilter();
        filter.setRating(4.0);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getRating()).isGreaterThanOrEqualTo(4.0))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withMaxGuestsFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setMaxGuests(3);
        Accommodation acc2 = accommodation();
        acc2.setMaxGuests(1);

        var filter = new AccommodationSearchFilter();
        filter.setMaxGuests(2);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getMaxGuests()).isGreaterThanOrEqualTo(2))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withHostIdFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.getHost().setHostId("444");

        var filter = new AccommodationSearchFilter();
        filter.setHostId("444");

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, accommodation()))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getHost().getHostId()).isEqualTo("444"))
                .verifyComplete();
    }


    @Test
    public void findAllByFilter_withSuperHostFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.getHost().setSuperHost(true);

        var filter = new AccommodationSearchFilter();
        filter.setSuperHost(true);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, accommodation()))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getHost().isSuperHost()).isTrue())
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withTypeFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setType(WHOLE_APARTMENT);
        Accommodation acc2 = accommodation();
        acc2.setType(HOSTEL_ROOM);
        Accommodation acc3 = accommodation();
        acc3.setType(WHOLE_LOFT);

        var filter = new AccommodationSearchFilter();
        filter.setType(List.of(WHOLE_APARTMENT, WHOLE_LOFT));

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2, acc3))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getType()).isIn(WHOLE_APARTMENT, WHOLE_LOFT))
                .assertNext(a -> assertThat(a.getType()).isIn(WHOLE_APARTMENT, WHOLE_LOFT))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withCountryFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.getAddress().setCountry("Italy");
        Accommodation acc2 = accommodation();
        acc2.getAddress().setCountry("Germany");

        var filter = new AccommodationSearchFilter();
        filter.setCountry("Italy");

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getAddress().getCountry()).isEqualTo("Italy"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withCityFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.getAddress().setCity("Milan");
        Accommodation acc2 = accommodation();
        acc2.getAddress().setCity("Berlin");

        var filter = new AccommodationSearchFilter();
        filter.setCity("Milan");

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getAddress().getCity()).isEqualTo("Milan"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withAmenitiesFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setAmenities(List.of(WIFI, KITCHEN, ESSENTIALS));
        Accommodation acc2 = accommodation();
        acc2.setAmenities(List.of(WIFI, FRIDGE));
        Accommodation acc3 = accommodation();
        acc3.setAmenities(List.of(WIFI, FRIDGE, PARKING_SPOT));

        var filter = new AccommodationSearchFilter();
        filter.setAmenities(List.of(WIFI, FRIDGE));

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2, acc3))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getAmenities()).contains(WIFI, FRIDGE))
                .assertNext(a -> assertThat(a.getAmenities()).contains(WIFI, FRIDGE))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withExcludeFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setAccommodationId("123");
        Accommodation acc2 = accommodation();
        acc2.setAccommodationId("456");
        Accommodation acc3 = accommodation();
        acc3.setAccommodationId("678");

        var filter = new AccommodationSearchFilter();
        filter.setExclude(List.of("678"));

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2, acc3))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> assertThat(a.getAccommodationId()).isNotEqualTo("678"))
                .assertNext(a -> assertThat(a.getAccommodationId()).isNotEqualTo("678"))
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withBoxFilter_shouldReturnFilteredResults() {

        Accommodation acc1 = accommodation();
        acc1.setAccommodationId("1");
        acc1.getAddress().setLocation(new GeoJsonPoint(9.16648, 45.45192));
        Accommodation acc2 = accommodation();
        acc2.setAccommodationId("2");
        acc2.getAddress().setLocation(new GeoJsonPoint(9.16773, 45.46777));
        Accommodation acc3 = accommodation();
        acc3.setAccommodationId("3");
        acc3.getAddress().setLocation(new GeoJsonPoint(9.15065, 45.44421));
        Accommodation acc4 = accommodation();
        acc4.setAccommodationId("4");
        acc4.getAddress().setLocation(new GeoJsonPoint(33.21848, 10.12651));

        var filter = new AccommodationSearchFilter();
        filter.setNe_lat(45.58727093761332);
        filter.setNe_lng(9.364992306814543);
        filter.setSw_lat(45.323709200302815);
        filter.setSw_lng(8.97003380233889);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2, acc3, acc4))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withPaging_shouldReturnPagedResults() {

        var filter = new AccommodationSearchFilter();
        filter.setPage(0);
        filter.setSize(2);

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(accommodation(), accommodation(), accommodation()))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findAllByFilter_withSorting_shouldReturnSortedResults() {

        Accommodation acc1 = accommodation();
        acc1.setPricePerNight(100.0);
        acc1.setMaxGuests(2);
        Accommodation acc2 = accommodation();
        acc2.setPricePerNight(80.0);
        Accommodation acc3 = accommodation();
        acc3.setPricePerNight(100.0);
        acc3.setMaxGuests(4);
        Accommodation acc4 = accommodation();
        acc4.setPricePerNight(50.0);

        var filter = new AccommodationSearchFilter();
        filter.setSortBy(List.of("pricePerNight", "maxGuests"));
        filter.setDirection("DESC");

        Flux<Accommodation> accommodations = repo
                .saveAll(Flux.just(acc1, acc2, acc3, acc4))
                .thenMany(repo.findAllByFilter(filter));

        StepVerifier
                .create(accommodations)
                .assertNext(a -> {
                    assertThat(a.getPricePerNight()).isEqualTo(100.0);
                    assertThat(a.getMaxGuests()).isEqualTo(4);
                })
                .assertNext(a -> {
                    assertThat(a.getPricePerNight()).isEqualTo(100.0);
                    assertThat(a.getMaxGuests()).isEqualTo(2);
                })
                .assertNext(a -> assertThat(a.getPricePerNight()).isEqualTo(80.0))
                .assertNext(a -> assertThat(a.getPricePerNight()).isEqualTo(50.0))
                .verifyComplete();
    }

    @Test
    public void findByAccommodationIdAndHostId() {

        Accommodation acc = accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("444");

        Mono<Accommodation> accommodation = repo.save(acc)
                .then(repo.findByAccommodationIdAndHostId("123", "444"));

        StepVerifier
                .create(accommodation)
                .assertNext(a -> {
                    assertThat(a.getAccommodationId()).isEqualTo("123");
                    assertThat(a.getHost().getHostId()).isEqualTo("444");
                })
                .verifyComplete();
    }
}

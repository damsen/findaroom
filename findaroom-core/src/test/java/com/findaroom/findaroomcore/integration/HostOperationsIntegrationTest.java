package com.findaroom.findaroomcore.integration;

import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.repo.AccommodationRepository;
import com.findaroom.findaroomcore.repo.BookingRepository;
import com.findaroom.findaroomcore.repo.ReviewRepository;
import com.findaroom.findaroomcore.utils.TestPojos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static com.findaroom.findaroomcore.model.enums.BookingStatus.DONE;
import static com.findaroom.findaroomcore.model.enums.BookingStatus.PENDING;
import static com.findaroom.findaroomcore.utils.MessageUtils.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class HostOperationsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccommodationRepository accommodationRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    public void setup() {
        accommodationRepo.deleteAll().block();
        bookingRepo.deleteAll().block();
        reviewRepo.deleteAll().block();
    }

    @Test
    public void getHostAccommodations() {

        Accommodation acc1 = TestPojos.accommodation();
        acc1.getHost().setHostId("andrea_damiani@protonmail.com");
        Accommodation acc2 = TestPojos.accommodation();
        accommodationRepo.saveAll(Flux.just(acc1, acc2)).blockLast();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/host-ops/my-accommodations")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].host.hostId", "andrea_damiani@protonmail.com").exists();
    }

    @Test
    public void getAccommodationBookings() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        accommodationRepo.save(acc).block();

        Booking book1 = TestPojos.booking();
        book1.setAccommodationId("123");
        Booking book2 = TestPojos.booking();
        book2.setAccommodationId("123");
        bookingRepo.saveAll(Flux.just(book1, book2)).blockLast();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Booking.class).hasSize(2);
    }

    @Test
    public void updateAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        accommodationRepo.save(acc).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        UpdateAccommodation update = TestPojos.updateAccommodation();
        update.setName("new");
        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}", "123")
                .contentType(APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.accommodationId", "123").exists()
                .jsonPath("@.name", "new").exists();
    }

    @Test
    public void confirmBooking() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        accommodationRepo.save(acc).block();

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        bookingRepo.save(book).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/confirm", "123", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.status", "CONFIRMED").exists();
    }

    @Test
    public void confirmBooking_whenBookingNotPending_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.setAccommodationId("123");
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        accommodationRepo.save(acc).block();

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setStatus(DONE);
        bookingRepo.save(book).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/confirm", "123", "111")
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("@.message", BOOKING_NOT_PENDING).exists();
    }

    @Test
    public void cancelBooking() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        bookingRepo.save(book).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/cancel", "123", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.status", "CANCELLED").exists();
    }

    @Test
    public void cancelBooking_whenBookingNotActive_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = TestPojos.booking();
        book.setBookingId("111");
        book.setAccommodationId("123");
        book.setStatus(DONE);
        bookingRepo.save(book).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings/{bookingId}/cancel", "123", "111")
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("@.message", BOOKING_NOT_ACTIVE).exists();
    }

    @Test
    public void unlistAccommodation() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        acc.setAccommodationId("123");
        accommodationRepo.save(acc).block();

        Booking book = TestPojos.booking();
        book.setAccommodationId("123");
        book.setStatus(PENDING);
        bookingRepo.save(book).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/unlist", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.listed", "false").exists();

        webTestClient
                .mutateWith(jwtMutator)
                .get()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/bookings", "123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("@.[0].status", "CANCELLED").exists();
    }

    @Test
    public void unlistAccommodation_whenAccommodationAlreadyUnlisted_shouldReturnUnprocessableEntity() {

        Accommodation acc = TestPojos.accommodation();
        acc.getHost().setHostId("andrea_damiani@protonmail.com");
        acc.setAccommodationId("123");
        acc.setListed(false);
        accommodationRepo.save(acc).block();

        var jwtMutator = mockJwt()
                .authorities(new SimpleGrantedAuthority("host"))
                .jwt(jwt -> jwt.claim("sub", "andrea_damiani@protonmail.com"));

        webTestClient
                .mutateWith(jwtMutator)
                .patch()
                .uri("/api/v1/host-ops/my-accommodations/{accommodationId}/unlist", "123")
                .exchange()
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("@.message", ACCOMMODATION_ALREADY_UNLISTED).exists();
    }
}

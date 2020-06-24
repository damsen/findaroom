package com.findaroom.findaroomcore.controller;

import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.dto.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.dto.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import com.findaroom.findaroomcore.service.HostOpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/host-ops")
@RequiredArgsConstructor
public class HostOpsController {

    private final HostOpsService hostOps;

    @GetMapping("/my-accommodations")
    public Flux<Accommodation> getHostAccommodations(AccommodationSearchFilter filter,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return hostOps.findAccommodationsByHostId(jwt.getSubject(), filter);
    }

    @GetMapping("/my-accommodations/{accommodationId}/bookings")
    public Flux<Booking> getAccommodationBookings(@PathVariable String accommodationId,
                                                  BookingSearchFilter filter,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return hostOps.findAccommodationBookingsByFilter(accommodationId, jwt.getSubject(), filter);
    }

    @PatchMapping("/my-accommodations/{accommodationId}")
    public Mono<Accommodation> updateAccommodation(@PathVariable String accommodationId,
                                                   @RequestBody @Valid UpdateAccommodation update,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return hostOps.updateAccommodation(accommodationId, jwt.getSubject(), update);
    }

    @PatchMapping("/my-accommodations/{accommodationId}/bookings/{bookingId}/confirm")
    public Mono<Booking> confirmBooking(@PathVariable String accommodationId,
                                        @PathVariable String bookingId,
                                        @AuthenticationPrincipal Jwt jwt) {
        return hostOps.confirmBooking(accommodationId, bookingId, jwt.getSubject());
    }

    @PatchMapping("/my-accommodations/{accommodationId}/bookings/{bookingId}/cancel")
    public Mono<Booking> cancelBooking(@PathVariable String accommodationId,
                                       @PathVariable String bookingId,
                                       @AuthenticationPrincipal Jwt jwt) {
        return hostOps.cancelBooking(accommodationId, bookingId, jwt.getSubject());
    }

    @PatchMapping("/my-accommodations/{accommodationId}/unlist")
    public Mono<Accommodation> unlistAccommodation(@PathVariable String accommodationId,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return hostOps.unlistAccommodation(accommodationId, jwt.getSubject());
    }
}

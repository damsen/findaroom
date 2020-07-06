package com.findaroom.findaroomcore.controller;

import com.findaroom.findaroomcore.controller.event.BookAccommodation;
import com.findaroom.findaroomcore.controller.event.BookingDates;
import com.findaroom.findaroomcore.controller.event.CreateAccommodation;
import com.findaroom.findaroomcore.controller.event.ReviewAccommodation;
import com.findaroom.findaroomcore.controller.filter.AccommodationSearchFilter;
import com.findaroom.findaroomcore.controller.filter.BookingSearchFilter;
import com.findaroom.findaroomcore.controller.filter.ReviewSearchFilter;
import com.findaroom.findaroomcore.domain.Accommodation;
import com.findaroom.findaroomcore.domain.Booking;
import com.findaroom.findaroomcore.domain.Review;
import com.findaroom.findaroomcore.service.UserOperationsService;
import com.findaroom.findaroomcore.utils.ClaimUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/user-ops")
@RequiredArgsConstructor
public class UserOperationsController {

    private final UserOperationsService userOps;

    @GetMapping("/my-bookings")
    public Flux<Booking> getUserBookings(BookingSearchFilter filter,
                                         @AuthenticationPrincipal Jwt jwt) {
        return userOps.findBookingsByUserId(jwt.getSubject(), filter);
    }

    @GetMapping("/my-reviews")
    public Flux<Review> getUserReviews(ReviewSearchFilter filter,
                                       @AuthenticationPrincipal Jwt jwt) {
        return userOps.findReviewsByUserId(jwt.getSubject(), filter);
    }

    @GetMapping("/my-favorites")
    public Flux<Accommodation> getUserFavorites(AccommodationSearchFilter filter,
                                                @AuthenticationPrincipal Jwt jwt) {
        return userOps.findUserFavorites(ClaimUtils.favorites(jwt), filter);
    }

    @GetMapping("/my-bookings/{bookingId}")
    public Mono<Booking> getUserBookingById(@PathVariable String bookingId,
                                            @AuthenticationPrincipal Jwt jwt) {
        return userOps.findUserBookingById(bookingId, jwt.getSubject());
    }

    @PostMapping("/accommodations")
    @ResponseStatus(CREATED)
    public Mono<Accommodation> saveAccommodation(@RequestBody @Valid CreateAccommodation create,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return userOps.saveAccommodation(jwt.getSubject(), ClaimUtils.superHost(jwt), create);
    }

    @PostMapping("/accommodations/{accommodationId}/bookings")
    @ResponseStatus(CREATED)
    public Mono<Booking> bookAccommodation(@PathVariable String accommodationId,
                                           @RequestBody @Valid BookAccommodation book,
                                           @AuthenticationPrincipal Jwt jwt) {
        return userOps.bookAccommodation(accommodationId, jwt.getSubject(), book);
    }

    @PostMapping("/accommodations/{accommodationId}/reviews")
    @ResponseStatus(CREATED)
    public Mono<Review> reviewAccommodation(@PathVariable String accommodationId,
                                            @RequestParam String bookingId,
                                            @RequestBody @Valid ReviewAccommodation review,
                                            @AuthenticationPrincipal Jwt jwt) {
        return userOps.reviewAccommodation(accommodationId, bookingId, jwt.getSubject(), review);
    }

    @PatchMapping("/my-bookings/{bookingId}/cancel")
    public Mono<Booking> cancelBooking(@PathVariable String bookingId,
                                       @AuthenticationPrincipal Jwt jwt) {
        return userOps.cancelBooking(bookingId, jwt.getSubject());
    }

    @PatchMapping("/my-bookings/{bookingId}/reschedule")
    public Mono<Booking> rescheduleBooking(@PathVariable String bookingId,
                                           @RequestBody @Valid BookingDates reschedule,
                                           @AuthenticationPrincipal Jwt jwt) {
        return userOps.rescheduleBooking(bookingId, jwt.getSubject(), reschedule);
    }
}

package com.findaroom.findaroomcore.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtils {

    public static final String ACCOMMODATION_NOT_FOUND = "Accommodation not found";
    public static final String USER_IS_ACCOMMODATION_HOST = "User is the host of the accommodation.";
    public static final String ACCOMMODATION_MAX_GUESTS_EXCEEDED = "Number of guests exceeds accommodation capacity.";
    public static final String ACCOMMODATION_ALREADY_UNLISTED = "Accommodation is already unlisted.";
    public static final String ACCOMMODATION_ALREADY_BOOKED = "Accommodation is already booked between selected dates.";

    public static final String BOOKING_NOT_FOUND = "Booking not found";
    public static final String BOOKING_NOT_COMPLETED = "Booking is not completed.";
    public static final String BOOKING_NOT_ACTIVE = "Booking is not active.";
    public static final String BOOKING_NOT_PENDING = "Booking is not pending.";
    public static final String BOOKING_DATES_SAME_AS_RESCHEDULE_DATES = "Booking dates and reschedule dates are the same.";

    public static final String USER_HAS_BOOKINGS_BETWEEN_DATES = "User already has bookings between selected dates.";

}

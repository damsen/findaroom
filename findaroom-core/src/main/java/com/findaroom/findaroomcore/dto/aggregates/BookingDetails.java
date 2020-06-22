package com.findaroom.findaroomcore.dto.aggregates;

import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Booking;
import lombok.Value;

@Value
public class BookingDetails {

    Accommodation accommodation;
    Booking booking;

    public static BookingDetails of(Booking booking, Accommodation accommodation){
        return new BookingDetails(accommodation, booking);
    }
}

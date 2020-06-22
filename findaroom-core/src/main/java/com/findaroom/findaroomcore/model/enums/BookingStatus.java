package com.findaroom.findaroomcore.model.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum BookingStatus {

    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    DONE("Done");

    private final String status;

    BookingStatus(String status) {
        this.status = status;
    }

    public static List<BookingStatus> activeStates() {
        return List.of(PENDING, CONFIRMED);
    }

    public static List<BookingStatus> completedStates() {
        return List.of(CANCELLED, DONE);
    }

}

package com.findaroom.findaroomcore.domain.enums;

import lombok.Getter;

@Getter
public enum AccommodationType {

    WHOLE_APARTMENT("Whole apartment"),
    WHOLE_LOFT("Whole loft"),
    WHOLE_CONDO("Whole condo"),
    PRIVATE_ROOM("Private room"),
    SHARED_ROOM("Shared room"),
    HOSTEL_ROOM("Hostel room"),
    HOTEL_ROOM("Hotel room");

    private final String type;

    AccommodationType(String type) {
        this.type = type;
    }
}


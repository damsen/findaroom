package com.findaroom.findaroomcore.domain.enums;

import lombok.Getter;

@Getter
public enum Amenity {

    WIFI("Wifi"),
    TV("TV"),
    AC("AC"),
    KITCHEN("Kitchen"),
    OVEN("Oven"),
    STOVE("Stove"),
    FRIDGE("Fridge"),
    WASHING_MACHINE("Washing Machine"),
    ESSENTIALS("Essentials"),
    ELEVATOR("Elevator"),
    PARKING_SPOT("Parking Spot");

    private final String amenity;

    Amenity(String amenity) {
        this.amenity = amenity;
    }
}


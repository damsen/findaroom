package com.findaroom.findaroomcore.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClaimUtils {

    public static String uid(Jwt jwt) {
        return jwt.getClaimAsString("uid");
    }

    public static Boolean superHost(Jwt jwt) {
        return jwt.getClaimAsBoolean("superHost");
    }

    public static List<String> favorites(Jwt jwt) {
        return jwt.getClaimAsStringList("favoriteAccommodations");
    }
}

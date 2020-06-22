package com.findaroom.findaroomcore.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.function.Consumer;

public class JwtUtils {

    public static Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "andrea_damiani@protonmail.com")
                .claim("superHost", false)
                .build();
    }

    public static Consumer<HttpHeaders> addJwt(Jwt jwt) {
        return headers -> headers.setBearerAuth(jwt.getTokenValue());
    }
}

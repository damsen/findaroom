package com.findaroom.findaroomcore.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorUtils {

    public static Supplier<ResponseStatusException> notFound(@Nullable String reason) {
        return () -> new ResponseStatusException(NOT_FOUND, reason);
    }

    public static Supplier<ResponseStatusException> unprocessableEntity(@Nullable String reason) {
        return () -> new ResponseStatusException(UNPROCESSABLE_ENTITY, reason);
    }
}

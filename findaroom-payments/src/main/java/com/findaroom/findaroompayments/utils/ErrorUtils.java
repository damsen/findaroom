package com.findaroom.findaroompayments.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorUtils {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Suppliers {

        public static Supplier<ResponseStatusException> notFound(@Nullable String reason) {
            return () -> new ResponseStatusException(NOT_FOUND, reason);
        }

        public static Supplier<ResponseStatusException> unprocessableEntity(@Nullable String reason) {
            return () -> new ResponseStatusException(UNPROCESSABLE_ENTITY, reason);
        }

        public static Supplier<ResponseStatusException> internalServerError(@Nullable String reason) {
            return () -> new ResponseStatusException(INTERNAL_SERVER_ERROR, reason);
        }

    }

    public static ResponseStatusException internalServerError(@Nullable String reason) {
        return new ResponseStatusException(INTERNAL_SERVER_ERROR, reason);
    }
}

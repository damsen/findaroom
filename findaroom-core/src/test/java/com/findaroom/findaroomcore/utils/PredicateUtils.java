package com.findaroom.findaroomcore.utils;

import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.function.Predicate;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class PredicateUtils {

    public static Predicate<Throwable> notFound(String reason) {
        return ex -> ex instanceof ResponseStatusException &&
                     Objects.equals(NOT_FOUND, ((ResponseStatusException) ex).getStatus()) &&
                     Objects.equals(reason, ((ResponseStatusException) ex).getReason());
    }

    public static Predicate<Throwable> unprocessableEntity(String reason) {
        return ex -> ex instanceof ResponseStatusException &&
                     Objects.equals(UNPROCESSABLE_ENTITY, ((ResponseStatusException) ex).getStatus()) &&
                     Objects.equals(reason, ((ResponseStatusException) ex).getReason());
    }
}

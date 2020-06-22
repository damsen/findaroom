package com.findaroom.findaroomcore.utils;

import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.function.Predicate;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class PredicateUtils {

    public static Predicate<Throwable> notFound(){
        return ex -> ex instanceof ResponseStatusException && Objects.equals(NOT_FOUND, ((ResponseStatusException) ex).getStatus());
    }

    public static Predicate<Throwable> unprocessableEntity(){
        return ex -> ex instanceof ResponseStatusException && Objects.equals(UNPROCESSABLE_ENTITY, ((ResponseStatusException) ex).getStatus());
    }
}

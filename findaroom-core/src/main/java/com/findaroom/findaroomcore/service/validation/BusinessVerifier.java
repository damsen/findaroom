package com.findaroom.findaroomcore.service.validation;

import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public interface BusinessVerifier {

    default <T> Mono<T> verify(T object, Predicate<T> predicate, @Nullable String reason) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(predicate);
        return Mono.just(object)
                .filter(predicate)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY, reason)));
    }

    default <T> Mono<T> verifyAsync(T object, Function<T, Mono<Boolean>> asyncPredicate, @Nullable String reason) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(asyncPredicate);
        return Mono.just(object)
                .filterWhen(asyncPredicate)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY, reason)));
    }
}

package com.findaroom.findaroomusers.user;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public interface ReactiveClient {

    default <T> Mono<T> onBoundedElastic(Callable<T> syncCall) {
        return Mono.fromCallable(syncCall)
                .onErrorMap(e -> new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getLocalizedMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

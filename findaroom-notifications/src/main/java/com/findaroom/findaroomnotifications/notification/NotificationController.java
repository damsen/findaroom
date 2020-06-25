package com.findaroom.findaroomnotifications.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepo notificationRepo;

    @GetMapping(produces = "application/stream+json")
    public Flux<Notification> getUserNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationRepo.findByUserId(jwt.getSubject());
    }

    @GetMapping("/{notificationId}")
    public Mono<Notification> getUserNotificationById(@PathVariable String notificationId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return notificationRepo
                .findByNotificationIdAndUserId(notificationId, jwt.getSubject())
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .doOnNext(notification -> notification.setSeen(true))
                .flatMap(notificationRepo::save);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<Notification> notifyUser(@RequestBody @Valid NotifyUser notify) {
        return notificationRepo.save(Notification.of(notify.getUserId(), notify.getMessage(), notify.getContentUrl()));
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> deleteNotificationById(@PathVariable String notificationId,
                                             @AuthenticationPrincipal Jwt jwt) {
        return notificationRepo.deleteByNotificationIdAndUserId(notificationId, jwt.getSubject());
    }
}

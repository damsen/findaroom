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
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    public static final String NOTIFICATION_NOT_FOUND = "Notification not found.";

    private final NotificationRepo notificationRepo;

    @GetMapping(produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<Notification> getUserNotifications(@AuthenticationPrincipal Jwt jwt) {
        return notificationRepo.findByUserId(jwt.getSubject());
    }

    @GetMapping("/{notificationId}")
    public Mono<Notification> getUserNotificationById(@PathVariable String notificationId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return notificationRepo
                .findByNotificationIdAndUserId(notificationId, jwt.getSubject())
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, NOTIFICATION_NOT_FOUND)))
                .flatMap(notification -> {
                    if (!notification.isSeen()) {
                        notification.setSeen(true);
                        return notificationRepo.save(notification);
                    }
                    return Mono.just(notification);
                });
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

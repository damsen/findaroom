package com.findaroom.findaroomnotifications.notification;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepo extends ReactiveMongoRepository<Notification, String> {

    Flux<Notification> findByUserId(String userId);

    Mono<Notification> findByNotificationIdAndUserId(String notificationId, String userId);

    Mono<Void> deleteByNotificationIdAndUserId(String notificationId, String userId);
}

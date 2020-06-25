package com.findaroom.findaroomnotifications.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MILLIS;

@Data
@Document(collection = "notifications")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    String notificationId;
    @Indexed
    String userId;
    String message;
    String contentUrl;
    @Indexed(direction = IndexDirection.DESCENDING)
    Instant createTime;
    boolean seen;

    public static Notification of(String userId, String message, String contentUrl) {
        return new Notification(null, userId, message, contentUrl, Instant.now().truncatedTo(MILLIS), false);
    }
}

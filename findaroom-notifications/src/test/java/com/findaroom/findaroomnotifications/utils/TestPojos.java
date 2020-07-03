package com.findaroom.findaroomnotifications.utils;

import com.findaroom.findaroomnotifications.notification.Notification;
import com.findaroom.findaroomnotifications.notification.NotifyUser;

public class TestPojos {

    public static Notification notification() {
        return Notification.of("andrea_damiani@protonmail.com", "message", "url");
    }

    public static NotifyUser notifyUser() {
        return new NotifyUser("andrea_damiani@protonmail.com", "message", "url");
    }
}

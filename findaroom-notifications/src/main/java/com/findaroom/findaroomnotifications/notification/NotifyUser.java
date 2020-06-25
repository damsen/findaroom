package com.findaroom.findaroomnotifications.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotifyUser {

    @NotBlank String userId;
    @NotBlank String message;
    @NotBlank String contentUrl;
}

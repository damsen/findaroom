package com.findaroom.findaroomusers.user;

import com.okta.sdk.resource.user.User;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Value(staticConstructor = "of")
public class PublicUser {

    String userId;
    Date created;
    String firstName;
    String lastName;
    String picture;
    String description;
    String profession;
    List<String> languages;
    Boolean superHost;
    String state;
    String city;

    public static PublicUser from(User user) {
        return PublicUser.of(
                user.getId(),
                user.getCreated(),
                user.getProfile().getFirstName(),
                user.getProfile().getLastName(),
                user.getProfile().getString("picture"),
                user.getProfile().getString("description"),
                user.getProfile().getString("profession"),
                user.getProfile().getStringList("languages"),
                user.getProfile().getBoolean("superHost"),
                user.getProfile().getString("state"),
                user.getProfile().getString("city")
        );
    }
}

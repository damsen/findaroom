package com.findaroom.findaroomusers.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfo {

    @NotNull String firstName;
    @NotNull String lastName;
    @NotNull String picture;
    @NotNull String description;
    @NotNull String profession;
    @NotNull List<String> languages;
    @NotNull String state;
    @NotNull String city;
    @NotNull String zipCode;
    @NotNull String streetAddress;

}

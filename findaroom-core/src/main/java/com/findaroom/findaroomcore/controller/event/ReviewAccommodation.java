package com.findaroom.findaroomcore.controller.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewAccommodation {

    @NotNull @DecimalMin("1.0") @DecimalMax("5.0") Double rating;
    @NotBlank String message;

}

package com.findaroom.findaroomcore.dto.aggregates;

import com.findaroom.findaroomcore.model.Accommodation;
import com.findaroom.findaroomcore.model.Review;
import lombok.Value;

import java.util.List;

@Value
public class AccommodationDetails {

    Accommodation accommodation;
    List<Review> reviews;
}

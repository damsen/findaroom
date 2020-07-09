package com.findaroom.findaroomcore.domain;

import com.findaroom.findaroomcore.controller.event.CreateAccommodation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {

    String imageId;
    String url;
    int width;
    int height;
    String deleteHash;

    public static Image from(CreateAccommodation.Image image){
        return new Image(image.getImageId(), image.getUrl(), image.getWidth(), image.getHeight(), image.getDeleteHash());
    }
}

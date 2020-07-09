package com.findaroom.findaroomimages.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {

    @JsonProperty("id")
    String imageId;
    @JsonProperty("link")
    String url;
    int width;
    int height;
    @JsonProperty("deletehash")
    String deleteHash;
}

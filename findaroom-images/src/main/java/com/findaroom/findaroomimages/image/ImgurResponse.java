package com.findaroom.findaroomimages.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImgurResponse {

    @JsonProperty("data")
    Image image;
    Boolean success;
    Integer status;
}

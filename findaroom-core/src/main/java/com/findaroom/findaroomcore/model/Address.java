package com.findaroom.findaroomcore.model;

import com.findaroom.findaroomcore.dto.CreateAccommodation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {

    String country;
    @Indexed
    String city;
    String zipcode;
    String street;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    GeoJsonPoint location;

    public static Address from(CreateAccommodation.Address address) {
        return new Address(
                address.getCountry(),
                address.getCity(),
                address.getZipcode(),
                address.getStreet(),
                address.getLocation().toGeoJsonPoint()
        );
    }
}

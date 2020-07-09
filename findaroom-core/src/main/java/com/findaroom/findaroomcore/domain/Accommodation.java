package com.findaroom.findaroomcore.domain;

import com.findaroom.findaroomcore.controller.event.CreateAccommodation;
import com.findaroom.findaroomcore.controller.event.UpdateAccommodation;
import com.findaroom.findaroomcore.domain.enums.AccommodationType;
import com.findaroom.findaroomcore.domain.enums.Amenity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Document(collection = "accommodations")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Accommodation {

    @Id
    String accommodationId;
    String name;
    String description;
    @Indexed
    double pricePerNight;
    double rating;
    int maxGuests;
    boolean listed;
    int restrooms;
    int bedrooms;
    int beds;
    Host host;
    AccommodationType type;
    Address address;
    List<Amenity> amenities;
    List<Image> images;

    public static Accommodation of(String name, String description, double pricePerNight, int maxGuests, int restrooms, int bedrooms, int beds, Host host, AccommodationType type, Address address, List<Amenity> amenities, List<Image> images) {
        return new Accommodation(null, name, description, pricePerNight, 0.0d, maxGuests, true, restrooms, bedrooms, beds, host, type, address, amenities, images);
    }

    public static Accommodation from(String userId, boolean superHost, CreateAccommodation create) {
        return Accommodation.of(
                create.getName(),
                create.getDescription(),
                create.getPricePerNight(),
                create.getMaxGuests(),
                create.getRestrooms(),
                create.getBedrooms(),
                create.getBeds(),
                new Host(userId, superHost),
                create.getType(),
                Address.from(create.getAddress()),
                create.getAmenities(),
                create.getImages().stream().map(Image::from).collect(Collectors.toList())
        );
    }

    public Accommodation updateWith(UpdateAccommodation update) {
        this.name = update.getName();
        this.description = update.getDescription();
        this.pricePerNight = update.getPricePerNight();
        this.maxGuests = update.getMaxGuests();
        this.restrooms = update.getRestrooms();
        this.bedrooms = update.getBedrooms();
        this.beds = update.getBeds();
        this.type = update.getType();
        this.amenities = update.getAmenities();
        return this;
    }

    public boolean fitsGuests(int guests) {
        return this.maxGuests >= guests;
    }

    public boolean hasHostWithId(String userId) {
        return Objects.equals(this.host.getHostId(), userId);
    }
}

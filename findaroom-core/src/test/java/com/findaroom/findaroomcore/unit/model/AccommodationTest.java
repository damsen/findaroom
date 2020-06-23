package com.findaroom.findaroomcore.unit.model;

import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.dto.UpdateAccommodation;
import com.findaroom.findaroomcore.model.Accommodation;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.PojoUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AccommodationTest {

    @Test
    public void fromCreate_shouldReturnAccommodationWithMatchingProperties() {

        CreateAccommodation create = createAccommodation();
        Accommodation acc = Accommodation.from("123", false, create);

        assertThat(acc.getAccommodationId()).isNull();
        assertThat(acc.getName()).isEqualTo(create.getName());
        assertThat(acc.getDescription()).isEqualTo(create.getDescription());
        assertThat(acc.getPricePerNight()).isEqualTo(create.getPricePerNight());
        assertThat(acc.getMaxGuests()).isEqualTo(create.getMaxGuests());
        assertThat(acc.getRestrooms()).isEqualTo(create.getRestrooms());
        assertThat(acc.getBedrooms()).isEqualTo(create.getBedrooms());
        assertThat(acc.getBeds()).isEqualTo(create.getBeds());
        assertThat(acc.getHost().getHostId()).isEqualTo("123");
        assertThat(acc.getHost().isSuperHost()).isFalse();
        assertThat(acc.getType()).isEqualTo(create.getType());
        assertThat(acc.getAddress().getCity()).isEqualTo(create.getAddress().getCity());
        assertThat(acc.getAddress().getCountry()).isEqualTo(create.getAddress().getCountry());
        assertThat(acc.getAddress().getZipcode()).isEqualTo(create.getAddress().getZipcode());
        assertThat(acc.getAddress().getStreet()).isEqualTo(create.getAddress().getStreet());
        assertThat(acc.getAddress().getLocation().getX()).isEqualTo(create.getAddress().getLocation().getX());
        assertThat(acc.getAddress().getLocation().getY()).isEqualTo(create.getAddress().getLocation().getY());
        assertThat(acc.getAmenities()).containsAll(create.getAmenities());
    }

    @Test
    public void updateWith_shouldReturnAccommodationWithUpdatedProperties() {

        UpdateAccommodation update = updateAccommodation();
        Accommodation updated = accommodation().updateWith(update);

        assertThat(updated.getName()).isEqualTo(update.getName());
        assertThat(updated.getDescription()).isEqualTo(update.getDescription());
        assertThat(updated.getPricePerNight()).isEqualTo(update.getPricePerNight());
        assertThat(updated.getMaxGuests()).isEqualTo(update.getMaxGuests());
        assertThat(updated.getRestrooms()).isEqualTo(update.getRestrooms());
        assertThat(updated.getBedrooms()).isEqualTo(update.getBedrooms());
        assertThat(updated.getBeds()).isEqualTo(update.getBeds());
        assertThat(updated.getType()).isEqualTo(update.getType());
        assertThat(updated.getAmenities()).containsAll(update.getAmenities());
    }

}

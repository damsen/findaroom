package com.findaroom.findaroomcore.unit.domain;

import com.findaroom.findaroomcore.controller.event.CreateAccommodation;
import com.findaroom.findaroomcore.domain.Address;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.TestPojos.createAddress;
import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {

    @Test
    public void from_shouldReturnAddressWithMatchingProperties() {

        CreateAccommodation.Address createAddress = createAddress();
        Address address = Address.from(createAddress);

        assertThat(address.getCountry()).isEqualTo(createAddress.getCountry());
        assertThat(address.getCity()).isEqualTo(createAddress.getCity());
        assertThat(address.getZipcode()).isEqualTo(createAddress.getZipcode());
        assertThat(address.getStreet()).isEqualTo(createAddress.getStreet());
        assertThat(address.getLocation()).isEqualTo(createAddress.getLocation().toGeoJsonPoint());
    }
}

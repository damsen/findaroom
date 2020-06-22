package com.findaroom.findaroomcore.unit.model;

import com.findaroom.findaroomcore.dto.CreateAccommodation;
import com.findaroom.findaroomcore.model.Host;
import org.junit.jupiter.api.Test;

import static com.findaroom.findaroomcore.utils.PojoUtils.createHost;
import static org.assertj.core.api.Assertions.assertThat;

public class HostTest {

    @Test
    public void from_shouldReturnHostWithMatchingProperties() {

        CreateAccommodation.Host createHost = createHost();
        Host host = Host.from(createHost);

        assertThat(host.getHostId()).isEqualTo(createHost.getHostId());
        assertThat(host.isSuperHost()).isEqualTo(createHost.getSuperHost());
    }
}

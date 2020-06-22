package com.findaroom.findaroomcore.model;

import com.findaroom.findaroomcore.dto.CreateAccommodation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Host {

    @Indexed
    String hostId;
    boolean superHost;

    public static Host from(CreateAccommodation.Host host) {
        return new Host(host.getHostId(), host.getSuperHost());
    }
}

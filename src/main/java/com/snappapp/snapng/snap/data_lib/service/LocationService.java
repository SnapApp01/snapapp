package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.LocationCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Location;
import org.springframework.stereotype.Component;

@Component
public interface LocationService {
    Location getLocation(Long id);
    Location addLocation(LocationCreationDto dto);
    void remove(Location location);
}

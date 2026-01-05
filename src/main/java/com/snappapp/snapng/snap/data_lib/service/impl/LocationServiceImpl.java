package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.LocationCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Location;
import com.snappapp.snapng.snap.data_lib.repositories.LocationRepository;
import com.snappapp.snapng.snap.data_lib.service.LocationService;
import com.snappapp.snapng.snap.utils.utilities.StringUtilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final LocationRepository repo;

    public LocationServiceImpl(LocationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Location getLocation(Long id) {
        return repo.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Location has not being saved"));
    }

    @Override
    public Location addLocation(LocationCreationDto dto) {
        Location location = new Location();
        location.setAddress(StringUtilities.trim(dto.getAddress()));
        location.setCity(StringUtilities.trim(dto.getCity()));
        location.setCountry(StringUtilities.trim(dto.getCountry()));
        location.setDescription(StringUtilities.trim(dto.getDescription()));
        location.setLandmark(StringUtilities.trim(dto.getLandmark()));
        location.setState(StringUtilities.trim(dto.getState()));
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        return repo.save(location);
    }

    @Override
    public void remove(Location location) {
        location.setActive(false);
        repo.save(location);
    }
}

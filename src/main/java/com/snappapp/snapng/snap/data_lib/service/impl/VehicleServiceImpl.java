package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.VehicleCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import com.snappapp.snapng.snap.data_lib.repositories.VehicleRepository;
import com.snappapp.snapng.snap.data_lib.service.VehicleService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repo;

    public VehicleServiceImpl(VehicleRepository repo) {
        this.repo = repo;
    }

    @Override
    public Vehicle getVehicle(String id) {
        return repo.findByVehicleIdAndActiveTrue(id).orElseThrow(()-> new ResourceNotFoundException("Vehicle not found"));
    }

    @Override
    public Vehicle createVehicle(VehicleCreationDto dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setAvailable(true);
        vehicle.setBusiness(dto.getBusiness());
        vehicle.setDescription(dto.getDescription());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setType(dto.getType());
        vehicle.setYear(dto.getYear());
        vehicle.setVehicleId(IdUtilities.shortUUID());
        return repo.save(vehicle);
    }

    @Override
    public List<Vehicle> getAvailableByType(VehicleType type) {
        return repo.findByActiveTrueAndAvailableTrueAndBusiness_IsOnlineTrueAndType(type);
    }

    @Override
    public void remove(Vehicle vehicle) {
        vehicle.setActive(false);
        repo.save(vehicle);
    }

    @Override
    public Vehicle changeVehicleAvailability(Vehicle vehicle, boolean availability) {
        vehicle.setAvailable(availability);
        return repo.save(vehicle);
    }

    @Override
    public List<Vehicle> getVehiclesForBusiness(Business business) {
        return repo.findByBusinessAndActiveTrue(business);
    }
}

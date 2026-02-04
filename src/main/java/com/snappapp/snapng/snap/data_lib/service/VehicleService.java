package com.snappapp.snapng.snap.data_lib.service;

import com.snappapp.snapng.snap.data_lib.dtos.VehicleCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface VehicleService {
    Vehicle getVehicleById(Long vehicleId);
    Vehicle getVehicle(String id);
    Vehicle createVehicle(VehicleCreationDto dto);
    List<Vehicle> getAvailableByType(VehicleType type);
    void remove(Vehicle vehicle);
    Vehicle changeVehicleAvailability(Vehicle vehicle,boolean availability);
    List<Vehicle> getVehiclesForBusiness(Business business);
}

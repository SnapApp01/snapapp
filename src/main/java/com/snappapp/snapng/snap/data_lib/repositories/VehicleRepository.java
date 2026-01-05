package com.snappapp.snapng.snap.data_lib.repositories;

import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,Long> {
    Optional<Vehicle> findByVehicleIdAndActiveTrue(String vehicleId);
    List<Vehicle> findByActiveTrueAndAvailableTrueAndBusiness_IsOnlineTrueAndType(VehicleType type);
    List<Vehicle> findByBusinessAndActiveTrue(Business business);
}

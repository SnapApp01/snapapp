package com.snappapp.snapng.snap.admin.services.serviceImpl;

import com.snappapp.snapng.snap.admin.apimodels.EnableIdRequest;
import com.snappapp.snapng.snap.admin.apimodels.VehicleApiResponse;
import com.snappapp.snapng.snap.admin.services.AdminVehicleService;
import com.snappapp.snapng.snap.api_lib.exceptions.ApiResponseCode;
import com.snappapp.snapng.snap.api_lib.exceptions.SnapApiException;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.repositories.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminVehicleServiceImpl implements AdminVehicleService {

    private final VehicleRepository vehicleRepository;

    public AdminVehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Page<VehicleApiResponse> getAll(Integer page, Integer size, String plateNumber) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Vehicle> pageResult;

        if (plateNumber != null && !plateNumber.isBlank()) {
            pageResult =
                    vehicleRepository.findByPlateNumberContainingIgnoreCase(
                            plateNumber, pageable
                    );
        } else {
            pageResult = vehicleRepository.findAll(pageable);
        }

        return pageResult.map(this::mapToResponse);
    }

    @Override
    public VehicleApiResponse getById(Long id) {

        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        if (vehicle.isEmpty()) {
            new SnapApiException("Vehicle not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        Vehicle vehicleEntity = vehicle.get();
        VehicleApiResponse vehicleApiResponse = mapToResponse(vehicleEntity);
        return vehicleApiResponse;
    }

    @Override
    public void changeStatus(EnableIdRequest request) {

        Optional<Vehicle> vehicle = vehicleRepository.findById(request.getId());
        if (vehicle.isEmpty()) {
            new SnapApiException("Vehicle not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        Vehicle vehicleEntity = vehicle.get();
        vehicleEntity.setAvailable(request.isEnabled());
        vehicleRepository.save(vehicleEntity);
    }

    private VehicleApiResponse mapToResponse(Vehicle vehicle) {

        return VehicleApiResponse.builder()
                .id(String.valueOf(vehicle.getId()))
                .plateNumber(vehicle.getPlateNumber())
                .type(
                        vehicle.getType() != null
                                ? vehicle.getType().name()
                                : null
                )
                .enabled(Boolean.TRUE.equals(vehicle.getAvailable()))
                .owner(
                        vehicle.getBusinessId() != null
                                ? String.valueOf(vehicle.getBusinessId())
                                : null
                )
                .build();
    }
}

package com.snappapp.snapng.snap.app_service.services;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.snap.app_service.apimodels.AddVehicleRequest;
import com.snappapp.snapng.snap.app_service.apimodels.VehicleRetrievalResponse;
import com.snappapp.snapng.snap.data_lib.dtos.VehicleCreationDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.DeliveryRequestService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.data_lib.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VehicleManagementService {

    private final VehicleService vehicleService;
    private final SnapUserService userService;
    private final BusinessService businessService;
    private final DeliveryRequestService requestService;

    public VehicleManagementService(VehicleService vehicleService, SnapUserService userService, BusinessService businessService, DeliveryRequestService requestService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.businessService = businessService;
        this.requestService = requestService;
    }

    public List<VehicleRetrievalResponse> getVehicles(Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new FailedProcessException("Only businesses have vehicles");
        }
        List<Vehicle> vehicles = vehicleService.getVehiclesForBusiness(business);
        vehicles.forEach(e->e.setAvailable(e.getAvailable() && !requestService.vehicleActiveRequest(e)));
        List<VehicleRetrievalResponse> responses = new ArrayList<>();
        vehicles.forEach(e->responses.add(VehicleRetrievalResponse.builder().vehicle(e).build()));
        return responses;
    }

    public List<VehicleRetrievalResponse> getAvailableVehicles(VehicleType type, Long userId){
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new FailedProcessException("Only businesses have vehicles");
        }
        List<Vehicle> vehicles = vehicleService.getVehiclesForBusiness(business);
        vehicles.removeIf(v->!v.getAvailable() || requestService.vehicleActiveRequest(v));
        List<VehicleRetrievalResponse> responses = new ArrayList<>();
        vehicles.forEach(e->responses.add(VehicleRetrievalResponse.builder().vehicle(e).build()));
        return responses;
    }

    public VehicleRetrievalResponse getVehicle(String vehicleId, Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new FailedProcessException("Only businesses have vehicles");
        }
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        if(vehicle.getBusiness().getCode().equalsIgnoreCase(business.getCode())){
            return VehicleRetrievalResponse.builder().vehicle(vehicle).build();
        }
        throw new FailedProcessException("Vehicle is not owned by this business");
    }

    public VehicleRetrievalResponse addVehicle(AddVehicleRequest request, Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new FailedProcessException("Only businesses can have vehicles");
        }
        Vehicle vehicle = vehicleService.createVehicle(VehicleCreationDto
                .builder()
                .business(business)
                .description(request.getDescription())
                .plateNumber(request.getPlateNumber())
                .type(request.getVehicle())
                .year(request.getYear())
                .build());
        return VehicleRetrievalResponse.builder().vehicle(vehicle).build();
    }

    public VehicleRetrievalResponse changeAvailability(String vehicleId, Long userId) {
        SnapUser user = userService.getUserById(userId);
        Business business = businessService.getBusinessOfUser(user);
        if(business==null){
            throw new FailedProcessException("Only businesses have vehicles");        }
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        if(vehicle.getBusiness().getCode().equalsIgnoreCase(business.getCode())){
            vehicle = vehicleService.changeVehicleAvailability(vehicle,!vehicle.getAvailable());
            return VehicleRetrievalResponse.builder().vehicle(vehicle).build();
        }
        throw new FailedProcessException("Vehicle is not owned by this business");
    }
}

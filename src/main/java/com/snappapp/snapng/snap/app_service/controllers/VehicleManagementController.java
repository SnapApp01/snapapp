package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.snap.app_service.apimodels.AddVehicleRequest;
import com.snappapp.snapng.snap.app_service.apimodels.VehicleRetrievalResponse;
import com.snappapp.snapng.snap.app_service.services.VehicleManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.enums.VehicleType;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleManagementController {

    private final VehicleManagementService service;
    private final SecurityUtil securityUtil;

    public VehicleManagementController(VehicleManagementService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public List<VehicleRetrievalResponse> getVehicles() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getVehicles(user.getId());
    }

    @GetMapping("/type/{type}")
    public List<VehicleRetrievalResponse> getVehicle(@PathVariable("type") VehicleType type) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getAvailableVehicles(type, user.getId());
    }

    @PostMapping
    public VehicleRetrievalResponse addVehicle(@Validated  @RequestBody AddVehicleRequest request) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.addVehicle(request,user.getId());
    }

    @PutMapping("/toggle/available/{vehicleId}")
    public VehicleRetrievalResponse toggleVehicle(@PathVariable("vehicleId") String vehicleId) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.changeAvailability(vehicleId, user.getId());
    }
}

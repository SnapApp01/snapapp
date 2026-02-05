package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.EnableIdRequest;
import com.snappapp.snapng.snap.admin.apimodels.VehicleApiResponse;
import com.snappapp.snapng.snap.admin.config.MockData;
import com.snappapp.snapng.snap.admin.services.AdminVehicleService;
import com.snappapp.snapng.snap.api_lib.exceptions.ApiResponseCode;
import com.snappapp.snapng.snap.api_lib.exceptions.SnapApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("api/v1/admin/vehicles")
@RestController
public class VehicleController {
    private final AdminVehicleService service;

    public VehicleController(AdminVehicleService service) {
        this.service = service;
    }

    @GetMapping
    public Page<VehicleApiResponse> getVehicles(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String plateNumber) {

        return service.getAll(page, size, plateNumber);
    }

    @GetMapping("/{id}")
    public VehicleApiResponse getVehicle(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/status")
    public void changeVehicleStatus(@RequestBody EnableIdRequest request) {
        service.changeStatus(request);
    }
}

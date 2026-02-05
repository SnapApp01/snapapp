package com.snappapp.snapng.snap.admin.services;

import com.snappapp.snapng.snap.admin.apimodels.EnableIdRequest;
import com.snappapp.snapng.snap.admin.apimodels.VehicleApiResponse;
import org.springframework.data.domain.Page;

public interface AdminVehicleService {
    Page<VehicleApiResponse> getAll(Integer page, Integer size, String plateNumber);

    VehicleApiResponse getById(Long id);

    void changeStatus(EnableIdRequest request);
}

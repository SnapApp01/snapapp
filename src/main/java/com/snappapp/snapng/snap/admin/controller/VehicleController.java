package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.EnableIdRequest;
import com.snappapp.snapng.snap.admin.apimodels.VehicleApiResponse;
import com.snappapp.snapng.snap.admin.config.MockData;
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
    @Autowired
    private MockData mockData;

    @GetMapping
    public Page<VehicleApiResponse> getVehicles(@RequestParam(value = "page",defaultValue = "0")Integer page,
                                                @RequestParam(value = "size",defaultValue = "5")Integer size,
                                                @RequestParam(value = "plateNumber", required = false)String plateNumber){
        List<VehicleApiResponse> ls = new ArrayList<>();
        mockData.getVehicleList().forEach((k,v)-> ls.add(v));
        return new PageImpl<>(ls.subList(page,Math.min(size,ls.size())), PageRequest.of(page,size),mockData.getVehicleList().size());
    }

    @GetMapping("/{id}")
    public VehicleApiResponse getVehicle(@PathVariable("id") String id) throws SnapApiException {
        VehicleApiResponse rsp = mockData.getVehicleList().get(id);
        if(rsp!=null){
            return rsp;
        }
        throw new SnapApiException("Vehicle not found", ApiResponseCode.ITEM_NOT_FOUND);
    }

    @PutMapping("/status")
    public void changeVehicleStatus(@RequestBody EnableIdRequest request)throws SnapApiException {
        VehicleApiResponse rsp = mockData.getVehicleList().get(request.getId());
        if(rsp==null){
            throw new SnapApiException("User with userid not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        rsp.setEnabled(request.isEnabled());
    }
}

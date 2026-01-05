package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.BlockIdRequest;
import com.snappapp.snapng.snap.admin.apimodels.IdRequest;
import com.snappapp.snapng.snap.admin.apimodels.PartnerApiResponse;
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

@RequestMapping("api/v1/admin/partners")
@RestController
public class PartnerController {
    @Autowired
    private MockData mockData;

    @GetMapping
    public Page<PartnerApiResponse> getUsers(@RequestParam(value = "page",defaultValue = "0")Integer page,
                                             @RequestParam(value = "size",defaultValue = "5")Integer size){
        List<PartnerApiResponse> ls = new ArrayList<>();
        mockData.getPartnerList().forEach((k,v)-> ls.add(v));
        return new PageImpl<>(ls.subList(page,Math.min(size,ls.size())), PageRequest.of(page,size),mockData.getUserList().size());
    }

    @GetMapping("/{id}")
    public PartnerApiResponse getUser(@PathVariable("id") String id) throws SnapApiException {
        PartnerApiResponse rsp = mockData.getPartnerList().get(id);
        if(rsp!=null){
            return rsp;
        }
        throw new SnapApiException("User with userid not found", ApiResponseCode.ITEM_NOT_FOUND);
    }

    @PutMapping("/status")
    public void changeUserStatus(@RequestBody BlockIdRequest request)throws SnapApiException {
        PartnerApiResponse rsp = mockData.getPartnerList().get(request.getId());
        if(rsp==null){
            throw new SnapApiException("Partner not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        rsp.setBlocked(request.getBlock());
    }

    @PutMapping("/verify")
    public void verifyPartner(@RequestBody IdRequest request) throws SnapApiException {
        PartnerApiResponse rsp = mockData.getPartnerList().get(request.getId());
        if(rsp==null){
            throw new SnapApiException("Partner not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        rsp.setVerified(true);
    }

    @GetMapping("/vehicles/{id}")
    public List<VehicleApiResponse> getVehicles(@PathVariable("id")String id) throws SnapApiException {
        PartnerApiResponse rsp = mockData.getPartnerList().get(id);
        if(rsp==null){
            throw new SnapApiException("Partner not found", ApiResponseCode.ITEM_NOT_FOUND);
        }
        List<VehicleApiResponse> ls = new ArrayList<>();
        mockData.getVehicleList().forEach((k,v)-> ls.add(v));
        return ls;
    }
}

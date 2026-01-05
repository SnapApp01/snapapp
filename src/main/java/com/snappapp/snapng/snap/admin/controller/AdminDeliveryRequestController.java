package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.DeliveryRequestApiResponse;
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

@RequestMapping("api/v1/admin/requests")
@RestController
public class AdminDeliveryRequestController {

    @Autowired
    private MockData mockData;

    @GetMapping
    public Page<DeliveryRequestApiResponse> getTransactions(@RequestParam(value = "page",defaultValue = "0")Integer page,
                                                            @RequestParam(value = "size",defaultValue = "10")Integer size){
        List<DeliveryRequestApiResponse> ls = new ArrayList<>();
        mockData.getRequestList().forEach((k,v)-> ls.add(v));
        return new PageImpl<>(ls.subList(page,Math.min(size,ls.size())), PageRequest.of(page,size),mockData.getRequestList().size());
    }

    @GetMapping("/{id}")
    public DeliveryRequestApiResponse getUser(@PathVariable("id") String id) throws SnapApiException {
        DeliveryRequestApiResponse rsp = mockData.getRequestList().get(id);
        if(rsp!=null){
            return rsp;
        }
        throw new SnapApiException("Request not found", ApiResponseCode.ITEM_NOT_FOUND);
    }
}

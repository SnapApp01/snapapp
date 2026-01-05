package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.TransactionApiResponse;
import com.snappapp.snapng.snap.admin.config.MockData;
import com.snappapp.snapng.snap.api_lib.exceptions.ApiResponseCode;
import com.snappapp.snapng.snap.api_lib.exceptions.SnapApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("api/v1/admin/transactions")
@RestController
public class TransactionController {

    @Autowired
    private MockData mockData;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<TransactionApiResponse> getTransactions(@RequestParam(value = "page",defaultValue = "0")Integer page,
                                                        @RequestParam(value = "size",defaultValue = "10")Integer size){
        List<TransactionApiResponse> ls = new ArrayList<>();
        mockData.getTransactionList().forEach((k,v)-> ls.add(v));
        return new PageImpl<>(ls.subList(page,Math.min(size,ls.size())), PageRequest.of(page,size),mockData.getTransactionList().size());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TransactionApiResponse getUser(@PathVariable("id") String id) throws SnapApiException {
        TransactionApiResponse rsp = mockData.getTransactionList().get(id);
        if(rsp!=null){
            return rsp;
        }
        throw new SnapApiException("Transaction not found", ApiResponseCode.ITEM_NOT_FOUND);
    }
}

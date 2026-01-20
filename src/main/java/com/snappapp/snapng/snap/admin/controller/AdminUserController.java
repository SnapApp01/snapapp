package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.snap.admin.apimodels.UserApiResponse;
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

@RequestMapping("api/v1/admin/users")
@RestController
public class AdminUserController {
    @Autowired
    private MockData mockData;

    @GetMapping
    public Page<UserApiResponse> getUsers(@RequestParam(value = "page",defaultValue = "0")Integer page,
                                          @RequestParam(value = "size",defaultValue = "5")Integer size){
        List<UserApiResponse> ls = new ArrayList<>();
        mockData.getUserList().forEach((k,v)-> ls.add(v));
        return new PageImpl<>(ls.subList(page,Math.min(size,ls.size())), PageRequest.of(page,size),mockData.getUserList().size());
    }

    @GetMapping("/{userId}")
    public UserApiResponse getUser(@PathVariable("userId") String id) throws SnapApiException {
        UserApiResponse rsp = mockData.getUserList().get(id);
        if(rsp!=null){
            return rsp;
        }
        throw new SnapApiException("User with userid not found", ApiResponseCode.ITEM_NOT_FOUND);
    }
//
//    @PutMapping("/status")
//    public void changeUserStatus(@RequestBody BlockIdRequest request)throws SnapApiException {
//        UserApiResponse rsp = mockData.getUserList().get(request.getId());
//        if(rsp==null){
//            throw new SnapApiException("User with userid not found", ApiResponseCode.ITEM_NOT_FOUND);
//        }
//        rsp.setBlocked(request.getBlock());
//    }

}

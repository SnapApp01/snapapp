package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.snap.app_service.apimodels.*;
import com.snappapp.snapng.snap.app_service.services.UserDetailService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.utills.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/user-details")
public class UserDetailController {

    private final UserDetailService userDetailService;
    private final SnapUserService userService;
    private final SecurityUtil securityUtil;

    public UserDetailController(UserDetailService userDetailService, SnapUserService userService, SecurityUtil securityUtil) {
        this.userDetailService = userDetailService;
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

//    @GetMapping
//    public UserDetailResponse get(@RequestHeader(Constants.HEADER_USER_ID) String userId){
//        return userDetailService.getUser(userId);
//    }

    @GetMapping
    public UserDetailResponse get() {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return userDetailService.getUser(user.getId());
    }

//
//    @PutMapping("/device/{token}")
//    public void saveToken(@RequestHeader(Constants.HEADER_USER_ID) String userId,
//                          @PathVariable(name = "token")String token){
//        userService.withDeviceKey(userId, token);
//    }
//
//@PutMapping("/device/{token}")
//public ResponseEntity<GenericResponse> saveToken(@PathVariable String token) {
//    SnapUser user = securityUtil.getCurrentLoggedInUser();
//    userService.withDeviceKey(user.getIdentifier(), token);
//    return new ResponseEntity<>(response, response.getHttpStatus());
//
//    return GenericResponse.builder()
//            .isSuccess(true)
//            .message("Device token saved successfully")
//            .httpStatus(HttpStatus.OK)
//            .build();
//}

    @PostMapping
    public ResponseEntity<GenericResponse> create(@RequestBody @Valid CreateUserDetailRequest request) {
        GenericResponse response = userDetailService.createUser(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

@PutMapping
public UserDetailResponse update(
        @Validated @RequestBody UpdateUserDetailRequest request) {

    SnapUser user = securityUtil.getCurrentLoggedInUser();
    userDetailService.updateUser(request, user.getId());

    return userDetailService.getUser(user.getId());
}

    @PutMapping("/device-key")
    public ResponseEntity<SnapUser> updateDeviceKey(
            @RequestParam String deviceKey) {
        SnapUser loggedInUser = securityUtil.getCurrentLoggedInUser();
        SnapUser user = userService.withDeviceKey(loggedInUser.getEmail(), deviceKey);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/business")
    public ResponseEntity<GenericResponse> createBusiness(@RequestBody @Valid CreateUserDetailWithBusinessRequest request) {
        GenericResponse response = userDetailService.createBusinessUser(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

@PutMapping("/business")
public UserDetailResponse addBusiness(
        @Validated @RequestBody AddBusinessRequest request) {

    SnapUser user = securityUtil.getCurrentLoggedInUser();
    userDetailService.addBusiness(request, user.getId());

    return userDetailService.getUser(user.getId());
}

@PutMapping("/business/{status}")
public UserDetailResponse updateBusinessOnlineStatus(
        @PathVariable Boolean status) {

    SnapUser user = securityUtil.getCurrentLoggedInUser();
    userDetailService.updateBusinessStatus(status, user.getId());

    return userDetailService.getUser(user.getId());
}

}

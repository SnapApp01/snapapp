package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/admin/users")
@RestController
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/allUsers")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        GenericResponse response = userService.getAllUsers(page, size);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> findById(@PathVariable Long id) {
        GenericResponse response = userService.getUserById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/toggleBusinessStatus/{id}")
    public ResponseEntity<GenericResponse> changeUserBusinessStatus(
            @PathVariable Long id) {
        GenericResponse response = userService.changeUserBusinessStatus(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        GenericResponse response = userService.updateUser(id, userUpdateRequest);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/disable/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> disableUser(@PathVariable Long id) {
        GenericResponse response = userService.toggleDisableUser(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
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

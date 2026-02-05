package com.snappapp.snapng.snap.admin.controller;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.services.UserService;
import com.snappapp.snapng.snap.admin.apimodels.AddRoleByEmailRequest;
import com.snappapp.snapng.snap.admin.services.AdminUserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/admin/users")
@RestController
public class AdminUserController {
    private final UserService userService;
    private final AdminUserRoleService adminUserRoleService;

    public AdminUserController(UserService userService, AdminUserRoleService adminUserRoleService) {
        this.userService = userService;
        this.adminUserRoleService = adminUserRoleService;
    }

    @PutMapping("/roles/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> addRoleToUser(
            @RequestBody @Valid AddRoleByEmailRequest request
    ) {

        adminUserRoleService.addRoleToUserByEmail(
                request.getEmail(),
                request.getRoleName()
        );

        GenericResponse response = new GenericResponse();
        response.setSuccess(true);
        response.setMessage("Role added successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        GenericResponse response = userService.getAllUsers(page, size);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> findById(@PathVariable Long id) {
        GenericResponse response = userService.getUserById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/toggleBusinessStatus/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> changeUserBusinessStatus(
            @PathVariable Long id) {
        GenericResponse response = userService.changeUserBusinessStatus(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

}

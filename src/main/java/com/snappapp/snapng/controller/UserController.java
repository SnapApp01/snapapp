package com.snappapp.snapng.controller;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Administration", description = "Endpoints for user management by admin")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> findById(@PathVariable Long id) {
        GenericResponse response = userService.getUserById(id);
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
}

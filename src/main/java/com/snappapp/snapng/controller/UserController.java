package com.snappapp.snapng.controller;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.UserUpdateRequest;
import com.snappapp.snapng.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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

    @PostMapping("/profilePicture/{userId}")
    public ResponseEntity<GenericResponse> uploadUserProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") String mediaType) {

        GenericResponse response = userService.uploadUserProfilePicture(userId, file, description, mediaType);
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

package com.snappapp.snapng.controller;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.*;
import com.snappapp.snapng.dto.response.CompleteResetRequest;
import com.snappapp.snapng.dto.token.RefreshTokenRequest;
import com.snappapp.snapng.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and account management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<GenericResponse> verifyEmail(@RequestParam String email, @RequestParam String code) {
        GenericResponse genericResponse = authService.verifyEmail(email, code);
        return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
    }

        @PostMapping("/login")
        public ResponseEntity<GenericResponse> login(@RequestBody LoginRequest loginRequest) {
            GenericResponse response =  authService.login(loginRequest);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }

        @PostMapping("/refresh")
        public ResponseEntity<GenericResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
            GenericResponse response = authService.refreshToken(request);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }

        @PostMapping("/change-password")
        public ResponseEntity<GenericResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
            GenericResponse genericResponse = authService.changePassword(request);
            return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
        }

        @DeleteMapping("/delete/{userId}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<GenericResponse> deleteUser(@PathVariable Long userId){
            GenericResponse genericResponse = authService.deleteAccount(userId);
            return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
        }

//        @PostMapping("/forgot-password")
//        public ResponseEntity<GenericResponse> forgotPassword(@Valid @RequestBody InitiatePasswordReset request) {
//            GenericResponse genericResponse = passwordResetService.initiatePasswordReset(request);
//            return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
//        }
//
//        @GetMapping("/validate-reset-token")
//        public ResponseEntity<GenericResponse> validateResetToken(@RequestParam String token) {
//            GenericResponse genericResponse = passwordResetService.validateResetToken(token);
//            return new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
//        }
//
//        @PostMapping("/reset-password")
//        public ResponseEntity<GenericResponse> resetPassword(@Valid @RequestBody CompleteResetRequest request) {
//            GenericResponse genericResponse = passwordResetService.completePasswordReset(request);
//            return  new ResponseEntity<>(genericResponse, genericResponse.getHttpStatus());
//        }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK.....");
    }

}

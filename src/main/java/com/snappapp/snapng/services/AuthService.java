package com.snappapp.snapng.services;


import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.authDTOS.ChangePasswordRequest;
import com.snappapp.snapng.dto.request.authDTOS.ContactUsRequest;
import com.snappapp.snapng.dto.request.authDTOS.LoginRequest;
import com.snappapp.snapng.dto.request.authDTOS.RegisterRequest;
import com.snappapp.snapng.dto.token.RefreshTokenRequest;
import org.springframework.stereotype.Component;

@Component
public interface AuthService {

    GenericResponse login(LoginRequest loginRequest);
    GenericResponse verifyEmail(String email, String code);
    GenericResponse changePassword(ChangePasswordRequest request);
    GenericResponse deleteAccount(Long userId);
    GenericResponse refreshToken(RefreshTokenRequest request);
}
package com.snappapp.snapng.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private boolean enabled;
    private String username;
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private List<String> roles;
}


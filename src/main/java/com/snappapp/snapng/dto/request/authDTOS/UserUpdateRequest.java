package com.snappapp.snapng.dto.request.authDTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    private String firstname;
    private String lastname;
    private String phoneNumber;
}

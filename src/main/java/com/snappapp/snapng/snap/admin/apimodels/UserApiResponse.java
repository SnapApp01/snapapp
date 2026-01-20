package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserApiResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private LocalDateTime registeredOn;
    private Integer requestCount;
    private Long walletBalance;
    private Boolean blocked;
}

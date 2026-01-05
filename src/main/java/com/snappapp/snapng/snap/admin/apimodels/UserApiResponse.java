package com.snappapp.snapng.snap.admin.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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

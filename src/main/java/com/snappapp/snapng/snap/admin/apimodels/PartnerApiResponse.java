package com.snappapp.snapng.snap.admin.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PartnerApiResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    private String phoneNumber;
    private LocalDateTime registeredOn;
    private Long walletBalance;
    private Boolean blocked;
    private Boolean verified;
}

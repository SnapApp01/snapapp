package com.snappapp.snapng.snap.admin.apimodels;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

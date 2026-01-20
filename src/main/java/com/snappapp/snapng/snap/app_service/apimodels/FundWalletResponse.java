package com.snappapp.snapng.snap.app_service.apimodels;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundWalletResponse {
    private String url;
    private String provider;
}

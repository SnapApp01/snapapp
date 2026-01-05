package com.snappapp.snapng.snap.app_service.apimodels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FundWalletResponse {
    private String url;
    private String provider;
}

package com.snappapp.snapng.snap.app_service.apimodels.transfer;

import lombok.Data;
import java.util.List;

@Data
public class BalanceResponse {
    private boolean status;
    private String message;
    private List<BalanceData> data;
    
    @Data
    public static class BalanceData {
        private String currency;
        private Long balance;
    }
}
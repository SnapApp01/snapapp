package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessDetailResponse {
    private String company;
    private boolean online;
    private boolean verified;
    @JsonIgnore
    private Long balanceInLong;
    @JsonIgnore
    private Long bookBalanceInLong;
    @JsonProperty("balance")
    public double balance(){
        return MoneyUtilities.fromMinorToDouble(this.balanceInLong);
    }

    @JsonProperty("bookBalance")
    public double bookBalance(){
        return MoneyUtilities.fromMinorToDouble(this.bookBalanceInLong);
    }
}

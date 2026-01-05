package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalculateMinimumCostResponse {
    @JsonIgnore
    private long costInLong;

    @JsonProperty("cost")
    public double cost(){
        return MoneyUtilities.fromMinorToDouble(costInLong);
    }
}

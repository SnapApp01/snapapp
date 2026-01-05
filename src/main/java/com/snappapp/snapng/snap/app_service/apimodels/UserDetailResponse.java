package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.utils.utilities.MoneyUtilities;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserDetailResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private BusinessDetailResponse business;
    @JsonIgnore
    private Long balanceInLong;
    @JsonIgnore
    private Long bookBalanceInLong;
    @JsonProperty("hasBusiness")
    public boolean hasBusiness(){
        return business!=null;
    }

    @JsonProperty("balance")
    public double balance(){
        return MoneyUtilities.fromMinorToDouble(this.balanceInLong);
    }

    @JsonProperty("bookBalance")
    public double bookBalance(){
        return MoneyUtilities.fromMinorToDouble(this.bookBalanceInLong);
    }
}

package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BankAccountResponse {
    @JsonIgnore
    private BankAccount bankAccount;

    @JsonProperty("accountNumber")
    public String getAccountNumber(){
        return bankAccount.getAccountNumber();
    }

    @JsonProperty("accountName")
    public String getAccountName(){
        return bankAccount.getAccountName();
    }

    @JsonProperty("bankCode")
    public String getBankCode(){
        return bankAccount.getBankCode();
    }

    @JsonProperty("bankName")
    public String getBankName(){
        return bankAccount.getBankName();
    }
}

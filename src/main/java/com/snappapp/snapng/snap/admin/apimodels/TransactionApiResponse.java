package com.snappapp.snapng.snap.admin.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.data_lib.enums.SnapUserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class TransactionApiResponse {
    private String id;
    private String email;
    private SnapUserType ownerType;
    @JsonIgnore
    private Boolean isDebit;
    private Long amount;
    private String description;
    private String owner;
    private LocalTime time;
    private LocalDate date;
    @JsonProperty("transactionType")
    public String getTransactionType(){
        return this.isDebit ? "DEBIT":"CREDIT";
    }
}

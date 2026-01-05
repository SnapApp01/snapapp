package com.snappapp.snapng.snap.data_lib.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateWalletTransferDto {
    private Long amount;
    private String debitWalletKey;
    private String creditWalletKey;
    private String narration;
    private String reference;

    public Long getDebitAmount(){
        return -1*amount;
    }
}

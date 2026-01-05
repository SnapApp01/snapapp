package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateWalletTransactionDto {
    private Wallet wallet;
    private Long amount;
    private String narration;
    private boolean isDebit;
    private String ref;
}

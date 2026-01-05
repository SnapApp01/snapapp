package com.snappapp.snapng.snap.data_lib.dtos;

import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatePaystackTransactionDto {
    private String narration;
    private Long amount;
    private Object data;
    private Wallet wallet;
}

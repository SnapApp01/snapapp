package com.snappapp.snapng.snap.data_lib.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdatePaystackTransactionDto {
    private String reference;
    private String providerRef;
    private String callbackUrl;
    private Object responseData;
    private Boolean isSuccessful;
}

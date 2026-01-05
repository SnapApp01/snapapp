package com.snappapp.snapng.snap.data_lib.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BankAccountDto {
    private String bankName;
    private String bankCode;
    private String accountNumber;
    private String accountName;
}

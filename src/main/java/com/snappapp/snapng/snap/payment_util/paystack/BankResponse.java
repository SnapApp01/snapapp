package com.snappapp.snapng.snap.payment_util.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankResponse {
    private String name;
    private String code;
}

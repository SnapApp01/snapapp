package com.snappapp.snapng.snap.payment_util.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InitializePaymentRequest {
    private String reference;
    @JsonProperty("callback_url")
    private String callbackUrl;
    private long amount;
    private String email;
    private String plan;
}

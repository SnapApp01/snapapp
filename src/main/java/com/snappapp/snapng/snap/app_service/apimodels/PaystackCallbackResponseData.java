package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackCallbackResponseData {
    private String reference;
    @JsonProperty("gateway_response")
    private String gatewayResponse;
    private String status;
    private Long amount;
    private Long fees;
    private String channel;
}

package com.snappapp.snapng.snap.payment_util.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericResponse<T>{
    private boolean status;
    private String message;
    private T data;
}

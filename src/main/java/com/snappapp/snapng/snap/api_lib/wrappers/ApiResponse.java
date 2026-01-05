package com.snappapp.snapng.snap.api_lib.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snappapp.snapng.snap.api_lib.exceptions.ApiResponseCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private boolean success;
    private String message;
    private Object response;
    @JsonIgnore
    private ApiResponseCode responseCode;

    @JsonProperty("responseCode")
    public String responseCodeString(){
        return responseCode.getCode();
    }

    public static ApiResponse successResponse(Object response){
        ApiResponse rsp = new ApiResponse();
        rsp.response = response;
        rsp.message = "Success";
        rsp.success = true;
        rsp.responseCode = ApiResponseCode.SUCCESS;
        return rsp;
    }

    public static ApiResponse errorResponse(String message, ApiResponseCode responseCode){
        ApiResponse rsp = new ApiResponse();
        rsp.message = message;
        rsp.responseCode = responseCode;
        rsp.success = false;
        return rsp;
    }
}

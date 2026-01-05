package com.snappapp.snapng.snap.api_lib.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SnapApiException extends Exception{
    private final ApiResponseCode responseCode;
    private final HttpStatus httpStatus;

    public SnapApiException(){
        super("There was an error. Please try again");
        this.httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        this.responseCode = ApiResponseCode.UNKNOWN_ERROR;
    }

    public SnapApiException(String message, ApiResponseCode responseCode, HttpStatus httpStatus) {
        super(message);
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
    }

    public SnapApiException(String message, ApiResponseCode responseCode){
        super(message);
        this.httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        this.responseCode = responseCode;
    }

    public SnapApiException(String message, HttpStatus status, Throwable ex, ApiResponseCode responseCode){
        super(message, ex);
        httpStatus = status;
        this.responseCode = responseCode;
    }
}

package com.snappapp.snapng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse {
    private boolean isSuccess;
    private String message;
    private HttpStatus httpStatus;
    private Object data;

    public GenericResponse(boolean isSuccess, String message, HttpStatus httpStatus) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.httpStatus = httpStatus;
    }


    public GenericResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;

        this.message = message;
    }
    public GenericResponse(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    public GenericResponse(String message) {
        this.message = message;
    }

    public GenericResponse(Object data) {
        this.data = data;
    }

    public GenericResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public GenericResponse(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public GenericResponse(String message, HttpStatus httpStatus, Object data) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.data = data;
    }

}
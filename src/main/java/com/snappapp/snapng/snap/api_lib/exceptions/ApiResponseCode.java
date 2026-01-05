package com.snappapp.snapng.snap.api_lib.exceptions;

public enum ApiResponseCode {
    SUCCESS("00"),UNKNOWN_ERROR("99"),INCORRECT_TASK("98"),
    INVALID_CLIENT("97"),NOT_REGISTERED("01"),
    BAD_REQUEST("02"),ITEM_NOT_FOUND("03"),ALREADY_EXIST("04");

    private String code;
    ApiResponseCode(String code){
        this.code = code;
    }
    public String getCode(){
        return this.code;
    }
}

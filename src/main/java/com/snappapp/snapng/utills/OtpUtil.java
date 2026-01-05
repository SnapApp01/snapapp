package com.snappapp.snapng.utills;

public class OtpUtil {

    public static String generateSixDigitCode() {
        int code = 100000 + (int)(Math.random() * 900000);
        return String.valueOf(code);
    }
}
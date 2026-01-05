package com.snappapp.snapng.snap.utils.utilities;


import com.google.api.client.util.Strings;

public class StringUtilities {
    public static String trim(String input){
        return Strings.isNullOrEmpty(input) ? null:input.trim();
    }
    public static String leftPadZeroes(long number, int len){
        return String.format("%0"+len+"d",number);
    }
}

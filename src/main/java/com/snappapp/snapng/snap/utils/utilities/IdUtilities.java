package com.snappapp.snapng.snap.utils.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdUtilities {
    private IdUtilities(){

    }

    private static AtomicInteger i = new AtomicInteger(0);
    private static int getValue(){
        return i.get()>=9999 ? i.getAndSet(0) : i.getAndIncrement();
    }
    public static String useUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    public static String shortUUID(){
        return useUUID().substring(0,8);
    }
    public static String useDateTimeAtomic(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))+String.format("%04d",getValue());
    }
    public static String useDateTime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    }
}

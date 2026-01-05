package com.snappapp.snapng.utills;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

public final class TimeBasedUserIdGenerator {

    private static final String PREFIX = "SNAP-USER-";

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static volatile int lastSecond = -1;

    private TimeBasedUserIdGenerator() {}

    public static synchronized String generate() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        int year = date.getYear() % 100;
        int dayOfYear = date.getDayOfYear();
        int secondOfDay = time.toSecondOfDay();

        if (secondOfDay != lastSecond) {
            SEQUENCE.set(0);
            lastSecond = secondOfDay;
        }

        int seq = SEQUENCE.getAndIncrement();
        if (seq > 999) {
            throw new IllegalStateException("ID generation overflow for this second");
        }

        return String.format(
                "%s%02d%03d%05d",
                PREFIX, year, dayOfYear, secondOfDay * 1000 + seq
        );
    }
}

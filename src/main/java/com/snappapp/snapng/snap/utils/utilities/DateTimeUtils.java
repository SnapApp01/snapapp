package com.snappapp.snapng.snap.utils.utilities;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DateTimeUtils {
    private static final String LAGOS_TIMEZONE = "Africa/Lagos";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    private DateTimeUtils() {
    }

    public static LocalDateTime parseDateTime(String dateTime){
        log.info(dateTime);
        log.info(DATE_FORMAT+" "+TIME_FORMAT);
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_FORMAT+" "+TIME_FORMAT));
    }

    public static LocalDate parseDate(String date){
        return LocalDate.parse(date,DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static LocalTime parseTime(String time){
        return LocalTime.parse(time,DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT+" "+TIME_FORMAT));
    }

    public static String formatDate(LocalDate date){
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(currentInstant());
    }

    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.ofInstant(currentInstant(), ZoneId.of("Africa/Lagos"));
    }

    private static Instant currentInstant() {
        return ZonedDateTime.now(ZoneId.of(LAGOS_TIMEZONE)).toInstant();
    }

    public static String timeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        if (seconds < 180) {
            return "few moments ago";
        }

        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " hours ago";
        }

        long days = duration.toDays();
        if (days < 7) {
            return days + " days ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " weeks ago";
        }

        long months = weeks / 4;
        if (months < 12) {
            return months + " months ago";
        }

        long years = months / 12;
        return years + " years ago";
    }
}

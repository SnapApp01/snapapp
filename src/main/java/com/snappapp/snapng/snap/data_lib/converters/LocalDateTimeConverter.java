package com.snappapp.snapng.snap.data_lib.converters;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Converter(autoApply = true)
@Getter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

    @Value("${database.time.zone:GMT+1}")
    private String timeZone;

    @Override
    public Date convertToDatabaseColumn(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        } else {
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        }
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Date date) {
        if (date == null) {
            return null;
        } else {
            Instant instant = Instant.ofEpochMilli(date.getTime());
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
    }
}


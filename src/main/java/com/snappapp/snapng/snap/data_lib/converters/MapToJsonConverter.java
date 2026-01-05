package com.snappapp.snapng.snap.data_lib.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Strings;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapToJsonConverter<T> implements AttributeConverter<T,String> {
    private final Class<T> clazz;

    @Override
    public String convertToDatabaseColumn(T map) {
        if(map==null)return null;
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public T convertToEntityAttribute(String s) {
        if(Strings.isNullOrEmpty(s))return null;
        try {
            return new ObjectMapper().readValue(s,clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

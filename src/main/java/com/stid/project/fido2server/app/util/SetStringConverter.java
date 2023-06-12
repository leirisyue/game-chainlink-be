package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class SetStringConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        try {
            String[] values = new ObjectMapper().readValue(dbData, String[].class);
            return Arrays.stream(values).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}

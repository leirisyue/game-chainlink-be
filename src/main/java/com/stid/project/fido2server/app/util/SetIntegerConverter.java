package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class SetIntegerConverter implements AttributeConverter<Set<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(Set<Integer> attribute) {
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<Integer> convertToEntityAttribute(String dbData) {
        try {
            Integer[] values = new ObjectMapper().readValue(dbData, Integer[].class);
            return Arrays.stream(values).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}

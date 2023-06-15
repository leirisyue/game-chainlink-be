package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.data.client.Origin;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class SetOriginConverter implements AttributeConverter<Set<Origin>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Origin> attribute) {
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<Origin> convertToEntityAttribute(String dbData) {
        try {
            Origin[] values = new ObjectMapper().readValue(dbData, Origin[].class);
            return Arrays.stream(values).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}

package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.converter.AuthenticatorTransportConverter;
import com.webauthn4j.data.AuthenticatorTransport;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class SetAuthenticatorTransportConverter implements AttributeConverter<Set<AuthenticatorTransport>, String> {
    private final AuthenticatorTransportConverter authenticatorTransportConverter;

    public SetAuthenticatorTransportConverter() {
        this.authenticatorTransportConverter = new AuthenticatorTransportConverter();
    }

    @Override
    public String convertToDatabaseColumn(Set<AuthenticatorTransport> attribute) {
        try {
            return new ObjectMapper()
                    .writeValueAsString(authenticatorTransportConverter.convertSetToStringSet(attribute));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<AuthenticatorTransport> convertToEntityAttribute(String dbData) {
        try {
            String[] values = new ObjectMapper()
                    .readValue(dbData, String[].class);
            return authenticatorTransportConverter
                    .convertSet(Arrays.stream(values)
                            .collect(Collectors.toSet()));
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}

package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class SetSubdomainConverter implements AttributeConverter<Set<RelyingParty.Subdomain>, String> {

    @Override
    public String convertToDatabaseColumn(Set<RelyingParty.Subdomain> attribute) {
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<RelyingParty.Subdomain> convertToEntityAttribute(String dbData) {
        try {
            RelyingParty.Subdomain[] values = new ObjectMapper().readValue(dbData, RelyingParty.Subdomain[].class);
            return Arrays.stream(values).collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}

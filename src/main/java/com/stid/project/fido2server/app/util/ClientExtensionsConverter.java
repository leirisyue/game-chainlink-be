package com.stid.project.fido2server.app.util;

import com.webauthn4j.converter.AuthenticationExtensionsClientOutputsConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ClientExtensionsConverter implements AttributeConverter<AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput>, String> {
    private final AuthenticationExtensionsClientOutputsConverter converter;

    public ClientExtensionsConverter(ObjectConverter objectConverter) {
        this.converter = new AuthenticationExtensionsClientOutputsConverter(objectConverter);
    }

    @Override
    public String convertToDatabaseColumn(AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> attribute) {
        try {
            return converter.convertToString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> convertToEntityAttribute(String dbData) {
        try {
            return converter.convert(dbData);
        } catch (Exception e) {
            return new AuthenticationExtensionsClientOutputs<>();
        }
    }
}

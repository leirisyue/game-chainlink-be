package com.stid.project.fido2server.app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class AuthenticatorExtensionsConverter implements AttributeConverter<AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput>, String> {
    private final JsonConverter jsonConverter;
    private final TypeReference<AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput>> typeReference = new TypeReference<>() {
    };

    public AuthenticatorExtensionsConverter(ObjectConverter objectConverter) {
        this.jsonConverter = objectConverter.getJsonConverter();
    }

    @Override
    public String convertToDatabaseColumn(AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> attribute) {
        return jsonConverter.writeValueAsString(attribute);
    }

    @Override
    public AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> convertToEntityAttribute(String dbData) {
        try {
            return jsonConverter.readValue(dbData, typeReference);
        } catch (Exception e) {
            return new AuthenticationExtensionsAuthenticatorOutputs<>();
        }
    }
}

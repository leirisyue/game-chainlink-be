/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stid.project.fido2server.app.util;

import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * AttestationStatementConverter
 */
@Converter
public class AttestationStatementConverter implements AttributeConverter<AttestationStatement, byte[]> {

    private final CborConverter cborConverter;

    public AttestationStatementConverter(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    @Override
    public byte[] convertToDatabaseColumn(AttestationStatement attribute) {
        AttestationStatementSerializationContainer container = new AttestationStatementSerializationContainer(attribute);
        return cborConverter.writeValueAsBytes(container);
    }

    @Override
    public AttestationStatement convertToEntityAttribute(byte[] dbData) {
        AttestationStatementSerializationContainer container = cborConverter.readValue(dbData, AttestationStatementSerializationContainer.class);
        return container != null ? container.attestationStatement() : null;
    }
}

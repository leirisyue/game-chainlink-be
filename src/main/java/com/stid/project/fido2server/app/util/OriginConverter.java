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

import com.webauthn4j.data.client.Origin;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OriginConverter implements AttributeConverter<Origin, String> {
    @Override
    public String convertToDatabaseColumn(Origin attribute) {
        return attribute.toString();
    }

    @Override
    public Origin convertToEntityAttribute(String dbData) {
        return new Origin(dbData);
    }
}

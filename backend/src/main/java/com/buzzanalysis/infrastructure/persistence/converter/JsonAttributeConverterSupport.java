package com.buzzanalysis.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON列(AttributeConverter)実装で共有するJacksonの {@link ObjectMapper} インスタンスを提供する。
 */
final class JsonAttributeConverterSupport {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonAttributeConverterSupport() {
    }
}

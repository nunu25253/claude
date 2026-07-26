package com.buzzanalysis.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@code Map<Integer, Double>} をJSON文字列列に変換するJPAコンバータ（投稿時間帯分布に使用）。 */
@Converter
public class IntegerDoubleMapJsonConverter implements AttributeConverter<Map<Integer, Double>, String> {

    @Override
    public String convertToDatabaseColumn(Map<Integer, Double> attribute) {
        try {
            return attribute == null ? "{}" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize map to JSON", e);
        }
    }

    @Override
    public Map<Integer, Double> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new LinkedHashMap<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<LinkedHashMap<Integer, Double>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to map", e);
        }
    }
}

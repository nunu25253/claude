package com.buzzanalysis.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** {@code List<String>} をJSON文字列列に変換するJPAコンバータ（Phase14の改善提案リスト等に使用）。 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize string list to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<String>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to string list", e);
        }
    }
}

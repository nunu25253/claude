package com.buzzanalysis.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** {@code List<UUID>} をJSON文字列列に変換するJPAコンバータ（伸びる投稿ランキングIDリストに使用）。 */
@Converter
public class UuidListJsonConverter implements AttributeConverter<List<UUID>, String> {

    @Override
    public String convertToDatabaseColumn(List<UUID> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize UUID list to JSON", e);
        }
    }

    @Override
    public List<UUID> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<UUID>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to UUID list", e);
        }
    }
}

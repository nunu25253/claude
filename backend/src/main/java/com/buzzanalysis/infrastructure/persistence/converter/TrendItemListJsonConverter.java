package com.buzzanalysis.infrastructure.persistence.converter;

import com.buzzanalysis.domain.trend.TrendItem;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** {@code List<TrendItem>}（Phase15のトレンド項目一覧）をJSON文字列列に変換するJPAコンバータ。 */
@Converter
public class TrendItemListJsonConverter implements AttributeConverter<List<TrendItem>, String> {

    @Override
    public String convertToDatabaseColumn(List<TrendItem> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize trend items to JSON", e);
        }
    }

    @Override
    public List<TrendItem> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<TrendItem>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to trend items", e);
        }
    }
}

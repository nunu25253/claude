package com.buzzanalysis.infrastructure.persistence.converter;

import com.buzzanalysis.domain.script.ScriptCut;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** {@code List<ScriptCut>}（Phase11の動画台本カット構成）をJSON文字列列に変換するJPAコンバータ。 */
@Converter
public class ScriptCutListJsonConverter implements AttributeConverter<List<ScriptCut>, String> {

    @Override
    public String convertToDatabaseColumn(List<ScriptCut> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize cuts to JSON", e);
        }
    }

    @Override
    public List<ScriptCut> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<ScriptCut>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to cuts", e);
        }
    }
}

package com.buzzanalysis.infrastructure.persistence.converter;

import com.buzzanalysis.domain.imageprompt.ImagePrompt;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** {@code List<ImagePrompt>}（Phase13の画像生成プロンプト一式）をJSON文字列列に変換するJPAコンバータ。 */
@Converter
public class ImagePromptListJsonConverter implements AttributeConverter<List<ImagePrompt>, String> {

    @Override
    public String convertToDatabaseColumn(List<ImagePrompt> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize prompts to JSON", e);
        }
    }

    @Override
    public List<ImagePrompt> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<ImagePrompt>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to prompts", e);
        }
    }
}

package com.buzzanalysis.infrastructure.persistence.converter;

import com.buzzanalysis.domain.carousel.CarouselPage;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** {@code List<CarouselPage>}（Phase12のカルーセルページ構成）をJSON文字列列に変換するJPAコンバータ。 */
@Converter
public class CarouselPageListJsonConverter implements AttributeConverter<List<CarouselPage>, String> {

    @Override
    public String convertToDatabaseColumn(List<CarouselPage> attribute) {
        try {
            return attribute == null ? "[]" : JsonAttributeConverterSupport.MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize pages to JSON", e);
        }
    }

    @Override
    public List<CarouselPage> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return JsonAttributeConverterSupport.MAPPER.readValue(dbData, new TypeReference<ArrayList<CarouselPage>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON to pages", e);
        }
    }
}

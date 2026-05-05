package com.creditflow.shared.infrastructure.persistence;

import com.creditflow.shared.domain.ApplicationNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApplicationNumberAttributeConverter implements AttributeConverter<ApplicationNumber, String> {

    @Override
    public String convertToDatabaseColumn(ApplicationNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ApplicationNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ApplicationNumber.of(dbData);
    }
}

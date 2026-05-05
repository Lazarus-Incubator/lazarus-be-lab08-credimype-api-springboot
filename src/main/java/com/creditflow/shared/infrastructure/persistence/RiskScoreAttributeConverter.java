package com.creditflow.shared.infrastructure.persistence;

import com.creditflow.shared.domain.RiskScore;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RiskScoreAttributeConverter implements AttributeConverter<RiskScore, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RiskScore attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public RiskScore convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : RiskScore.of(dbData);
    }
}

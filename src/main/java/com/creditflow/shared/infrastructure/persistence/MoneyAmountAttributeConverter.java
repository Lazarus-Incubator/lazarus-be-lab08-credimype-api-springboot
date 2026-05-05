package com.creditflow.shared.infrastructure.persistence;

import com.creditflow.shared.domain.MoneyAmount;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

@Converter(autoApply = true)
public class MoneyAmountAttributeConverter implements AttributeConverter<MoneyAmount, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(MoneyAmount attribute) {
        return attribute == null ? null : attribute.toBigDecimal();
    }

    @Override
    public MoneyAmount convertToEntityAttribute(BigDecimal dbData) {
        return MoneyAmount.ofNullable(dbData);
    }
}

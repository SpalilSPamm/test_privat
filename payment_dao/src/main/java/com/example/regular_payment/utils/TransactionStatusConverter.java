package com.example.regular_payment.utils;

import com.example.regular_payment.utils.enums.TransactionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, String> {
    @Override
    public String convertToDatabaseColumn(TransactionStatus status) {
        if (status == null) {
            return null;
        }
        return status.getStatusCode();
    }

    @Override
    public TransactionStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(TransactionStatus.values())
                .filter(s -> s.getStatusCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown TransactionStatus code: " + code));
    }
}

package com.example.regular_payment.utils.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {

    ACTIVE("A"),

    REVERSED("S");

    private final String statusCode;

    TransactionStatus(String statusCode) {
        this.statusCode = statusCode;
    }
}

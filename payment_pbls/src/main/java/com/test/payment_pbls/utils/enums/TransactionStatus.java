package com.test.payment_pbls.utils.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {

    ACTIVE("A"),

    REVERSED("S");

    private final String statusCode;

    TransactionStatus(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public static TransactionStatus fromCode(String code) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.statusCode.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown transaction status code: " + code);
    }
}

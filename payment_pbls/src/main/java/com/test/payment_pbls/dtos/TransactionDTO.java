package com.test.payment_pbls.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDTO(
        Long id,
        Long instructionId,
        String idempotencyId,
        BigDecimal amount,
        OffsetDateTime transactionTime,
        String transactionStatus
) {
}

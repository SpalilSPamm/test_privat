package com.example.regular_payment.dtos;

import com.example.regular_payment.models.Instruction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionCreateDTO(
        Instruction instruction,
        String idempotencyId,
        BigDecimal amount,
        OffsetDateTime transactionTime,
        String transactionStatus
) {
}

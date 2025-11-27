package com.test.payment_pbls.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class Transaction {

    private Long id;

    private Instruction instruction;

    private String idempotencyId;

    private BigDecimal amount;

    private OffsetDateTime transactionTime;

    private String transactionStatus;
}

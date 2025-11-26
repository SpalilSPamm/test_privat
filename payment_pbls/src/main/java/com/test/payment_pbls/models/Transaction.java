package com.test.payment_pbls.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

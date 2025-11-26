package com.example.regular_payment.models;

import com.example.regular_payment.utils.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "payment_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instruction_id")
    private Instruction instruction;

    private String idempotencyId;

    private BigDecimal amount;

    private OffsetDateTime transactionTime;

    @Column(name = "transaction_status", length = 1)
    private String transactionStatus;
}

package com.example.regular_payment.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "payment_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruction_id")
    private Instruction instruction;

    private String idempotencyId;

    private BigDecimal amount;

    private OffsetDateTime transactionTime;

    @Column(name = "transaction_status", length = 1)
    private String transactionStatus;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version) && Objects.equals(instruction, that.instruction) && Objects.equals(idempotencyId, that.idempotencyId) && Objects.equals(amount, that.amount) && Objects.equals(transactionTime, that.transactionTime) && Objects.equals(transactionStatus, that.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, instruction, idempotencyId, amount, transactionTime, transactionStatus);
    }
}

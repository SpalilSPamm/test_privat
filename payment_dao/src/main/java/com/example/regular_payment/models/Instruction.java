package com.example.regular_payment.models;

import com.example.regular_payment.utils.enums.InstructionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "payment_instruction")
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payer_first_name")
    private String payerFirstName;

    @Column(name = "payer_second_name")
    private String payerSecondName;

    @Column(name = "payer_patronymic")
    private String payerPatronymic;

    @Column(name = "payer_iin")
    private String payerIin;

    @Column(name = "payer_card_number")
    private String payerCardNumber;

    @Column(name = "recipient_settlement_account")
    private String recipientSettlementAccount;

    @Column(name = "recipient_bank_code")
    private String recipientBankCode;

    @Column(name = "recipient_edrpou")
    private String recipientEdrpou;

    @Column(name = "recipient_name")
    private String recipientName;

    private BigDecimal amount;

    @Column(name = "period_value")
    private Integer periodValue;

    @Column(name = "period_unit")
    @Enumerated(EnumType.STRING)
    private ChronoUnit periodUnit;

    @Column(name = "last_execution_at")
    private OffsetDateTime lastExecutionAt;

    @Column(name = "next_execution_at")
    private OffsetDateTime nextExecutionAt;

    @Column(name = "instruction_status")
    @Enumerated(EnumType.STRING)
    private InstructionStatus instructionStatus;

    @OneToMany(mappedBy = "instruction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

}

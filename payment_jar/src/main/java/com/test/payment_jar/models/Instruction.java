package com.test.payment_jar.models;

import com.test.payment_jar.utils.enums.InstructionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class Instruction {

    private Long id;

    private String payerFirstName;

    private String payerSecondName;

    private String payerPatronymic;

    private String payerIin;

    private String payerCardNumber;

    private String recipientSettlementAccount;

    private String recipientBankCode;

    private String recipientEdrpou;

    private String recipientName;

    private BigDecimal amount;

    private Integer periodValue;

    private ChronoUnit periodUnit;

    private OffsetDateTime lastExecutionAt;

    private OffsetDateTime nextExecutionAt;

    private InstructionStatus instructionStatus;

}

package com.example.regular_payment.dtos;

import com.example.regular_payment.utils.enums.InstructionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public record InstructionCreateDTO(String payerFirstName,
                                   String payerSecondName,
                                   String payerPatronymic,
                                   String payerIin,
                                   String payerCardNumber,
                                   String recipientSettlementAccount,
                                   String recipientBankCode,
                                   String recipientEdrpou,
                                   String recipientName,
                                   BigDecimal amount,
                                   Integer periodValue,
                                   ChronoUnit periodUnit,
                                   OffsetDateTime lastExecutionAt,
                                   OffsetDateTime nextExecutionAt,
                                   InstructionStatus instructionStatus) {
}

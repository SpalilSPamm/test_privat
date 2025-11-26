package com.test.payment_pbls.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

public record InstructionDTO(
        @NotBlank(message = "Payer first name should not be empty")
        @Pattern(regexp = "^[A-ZА-ЩЬЮЯЄІЇҐЁЭЫЪ][a-zа-щьюяєіїґA-ZА-ЩЬЮЯЄІЇҐёэыъA-ZА-ЯЁЭЫЪ'ʼ’\\s-]+$",
        message = "Payer first name is not correct")
        @Size(min = 1, max = 255, message = "Payer first is too short or too long")
        String payerFirstName,
        @NotBlank(message = "Payer second name should not be empty")
        @Pattern(regexp = "^[A-ZА-ЩЬЮЯЄІЇҐЁЭЫЪ][a-zа-щьюяєіїґA-ZА-ЩЬЮЯЄІЇҐёэыъA-ZА-ЯЁЭЫЪ'ʼ’\\s-]+$",
                message = "Payer second name is not correct")
        @Size(min = 1, max = 255, message = "Payer second is too short or too long")
        String payerSecondName,
        @NotBlank(message = "Payer patronymic  should not be empty")
        @Pattern(regexp = "^[A-ZА-ЩЬЮЯЄІЇҐЁЭЫЪ][a-zа-щьюяєіїґA-ZА-ЩЬЮЯЄІЇҐёэыъA-ZА-ЯЁЭЫЪ'ʼ’\\s-]+$",
                message = "Payer patronymic is not correct")
        @Size(min = 1, max = 255, message = "Payer patronymic is too short or too long")
        String payerPatronymic,
        @Pattern(regexp = "^\\d{10}$", message = "Payer IIN must be exactly 10 digits")
        String payerIin,
        @Pattern(regexp = "^\\d{16}$", message = "Payer card number must be exactly 16 digits")
        String payerCardNumber,
        @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z\\d]{25}$",
                message = "Recipient Settlement Account must be a valid IBAN of exactly 29 characters.")
        String recipientSettlementAccount,
        @Pattern(regexp = "^\\d{6}$", message = "Recipient bank code must be exactly 6 digits")
        String recipientBankCode,
        @Pattern(regexp = "^\\d{8}$", message = "Recipient EDRPOU must be exactly 8 digits")
        String recipientEdrpou,
        @NotBlank(message = "Recipient name should not be empty")
        @Pattern(regexp = "^[A-ZА-ЩЬЮЯЄІЇҐЁЭЫЪ][a-zа-щьюяєіїґA-ZА-ЩЬЮЯЄІЇҐёэыъA-ZА-ЯЁЭЫЪ'ʼ’\\s-]+$",
                message = "Payer first name is not correct")
        @Size(min = 1, max = 255, message = "Payer first is too short or too long")
        String recipientName,
        @NotNull(message = "Amount cannot be empty")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotNull(message = "Period value cannot be empty")
        @Min(value = 1, message = "Period value must be at least 1")
        Integer periodValue,
        @NotNull(message = "Period unit cannot be empty")
        ChronoUnit periodUnit
) {
}

package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.services.ValidationService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationServiceImplTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationServiceImpl();
    }

    @Test
    void shouldPassValidation_CustomWeights_Sample1() {
        String validIIN = "1111111118";
        assertDoesNotThrow(() -> validationService.validatePayerIinChecksum(validIIN));
    }

    @Test
    void shouldPassValidation_CustomWeights_Sample2_Mod11ToZero() {
        String validIIN = "1234567899";
        assertDoesNotThrow(() -> validationService.validatePayerIinChecksum(validIIN));
    }

    @Test
    void shouldFailValidation_CustomWeights_InvalidChecksum() {
        String invalidIIN = "1234567895";
        assertThrows(ValidationException.class,
                () -> validationService.validatePayerIinChecksum(invalidIIN));
    }

    @Test
    void shouldFailWhenIINIsNull() {
        assertThrows(ValidationException.class, () -> validationService.validatePayerIinChecksum(null));
    }

    @Test
    void shouldFailWhenIINIsTooShort() {
        assertThrows(ValidationException.class, () -> validationService.validatePayerIinChecksum("123456789"));
    }

    @Test
    void shouldFailWhenChecksumIsInvalid() {

        String invalidChecksumIIN = "2741548652";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validatePayerIinChecksum(invalidChecksumIIN));

        assert(exception.getMessage().contains("Invalid IIN checksum"));
    }

    @Test
    void shouldPassValidation_BaseWeights() {
        String validEdrpou = "00000017";
        assertDoesNotThrow(() -> validationService.validatePayerEdrpouChecksum(validEdrpou));
    }

    @Test
    void shouldPassValidation_AnotherBaseWeights() {
        String validEdrpou = "11111122";
        assertDoesNotThrow(() -> validationService.validatePayerEdrpouChecksum(validEdrpou));
    }

    @Test
    void shouldPassValidation_UsingAlternativeWeights() {

        assertDoesNotThrow(() -> validationService.validatePayerEdrpouChecksum("00000017"));
    }

    @Test
    void shouldFailWhenEDRPOUIsTooShort() {
        assertThrows(ValidationException.class, () -> validationService.validatePayerEdrpouChecksum("1234567"));
    }

    @Test
    void shouldFailWhenEDRPOUIsTooLong() {
        assertThrows(ValidationException.class, () -> validationService.validatePayerEdrpouChecksum("123456789"));
    }

    @Test
    void shouldFailWhenChecksumIsInvalid_Edrpou() {

        String invalidEdrpou = "00000018";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validatePayerEdrpouChecksum(invalidEdrpou));

        assert(exception.getMessage().contains("Invalid EDRPOU checksum"));
    }
}

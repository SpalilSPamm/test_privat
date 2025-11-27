package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.InstructionClient;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.InstructionCreateDTO;
import com.test.payment_pbls.dtos.InstructionValidDTO;
import com.test.payment_pbls.services.ValidationService;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstructionServiceImlTest {

    @Mock
    private InstructionClient instructionClient;

    @Mock
    private ValidationService validationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private InstructionServiceImpl instructionService;

    private static final String VALID_IIN = "1111111118";
    private static final String VALID_EDRPOU = "40087654";

    private static final Instant FIXED_INSTANT = Instant.parse("2025-10-06T10:00:00Z");
    private static final ZoneOffset TIME_ZONE = ZoneOffset.ofHours(2);
    private static final Long GENERATED_ID = 500L;


    @Test
    void shouldCreateInstructionSuccessfullyAndCallPdsClient() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        InstructionValidDTO dto = createValidInstructionDTO();
        Instruction mockReturnedEntity = createValidInstruction();

        when(instructionClient.createInstruction(any(InstructionCreateDTO.class))).thenReturn( mockReturnedEntity);

        Instruction result = instructionService.createInstruction(dto);

        verify(validationService, times(1)).validatePayerIinChecksum(dto.payerIin());
        verify(validationService, times(1)).validatePayerEdrpouChecksum(dto.recipientEdrpou());

        verify(instructionClient, times(1)).createInstruction(any(InstructionCreateDTO.class));

        assertEquals(GENERATED_ID, result.getId(), "ID має бути згенерований PDS.");
    }

    @Test
    void shouldThrowExceptionAndNotCallPdsClientWhenIinChecksumIsInvalid() {

        InstructionValidDTO dto = createValidInstructionDTO();

        doThrow(new ValidationException("Invalid IIN checksum."))
                .when(validationService).validatePayerIinChecksum(dto.payerIin());

        assertThrows(ValidationException.class, () -> instructionService.createInstruction(dto));

        verify(instructionClient, never()).createInstruction(any(InstructionCreateDTO.class));

        verify(validationService, never()).validatePayerEdrpouChecksum(any());
    }

    @Test
    void shouldThrowExceptionAndNotCallPdsClientWhenEdrpouChecksumIsInvalid() {

        InstructionValidDTO dto = createValidInstructionDTO();

        doThrow(new ValidationException("Invalid EDRPOU checksum."))
                .when(validationService).validatePayerEdrpouChecksum(dto.recipientEdrpou());

        assertThrows(ValidationException.class, () -> instructionService.createInstruction(dto));

        verify(instructionClient, never()).createInstruction(any(InstructionCreateDTO.class));

        verify(validationService, times(1)).validatePayerIinChecksum(dto.payerIin());
    }

    @Test
    void shouldThrowExceptionWhenInstructionClientGetProblem() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        InstructionValidDTO dto = createValidInstructionDTO();

        doThrow(new CreationFailureException("Client request failed."))
                .when(instructionClient).createInstruction(any(InstructionCreateDTO.class));

        assertThrows(CreationFailureException.class, () -> instructionService.createInstruction(dto));

        verify(validationService, times(1)).validatePayerIinChecksum(dto.payerIin());
        verify(validationService, times(1)).validatePayerEdrpouChecksum(dto.recipientEdrpou());

        verify(instructionClient, times(1)).createInstruction(any(InstructionCreateDTO.class));

    }

    @Test
    void getInstructionsByPayerIin_shouldPassValidationAndReturnInstructionsFromClient() {

        List<Instruction> expectedList = List.of(createValidInstruction());

        doNothing().when(validationService).validatePayerIinChecksum(eq(VALID_IIN));

        when(instructionClient.getInstructionsForIin(eq(VALID_IIN)))
                .thenReturn(expectedList);

        List<Instruction> result = instructionService.getInstructionsByPayerIin(VALID_IIN);

        assertFalse(result.isEmpty());
        assertEquals(expectedList.size(), result.size());

        verify(validationService, times(1)).validatePayerIinChecksum(eq(VALID_IIN));

        verify(instructionClient, times(1)).getInstructionsForIin(eq(VALID_IIN));
    }

    @Test
    void getInstructionsByPayerIin_shouldPassValidationAndReturnEmptyList() {

        List<Instruction> expectedList = Collections.emptyList();

        doNothing().when(validationService).validatePayerIinChecksum(eq(VALID_IIN));

        when(instructionClient.getInstructionsForIin(eq(VALID_IIN)))
                .thenReturn(expectedList);

        List<Instruction> result = instructionService.getInstructionsByPayerIin(VALID_IIN);

        assertTrue(result.isEmpty());
        verify(validationService, times(1)).validatePayerIinChecksum(eq(VALID_IIN));
        verify(instructionClient, times(1)).getInstructionsForIin(eq(VALID_IIN));
    }

    @Test
    void getInstructionsByPayerIin_shouldThrowValidationExceptionAndNotCallClientWhenChecksumIsInvalid() {

        String INVALID_IIN = "1234567895";

        doThrow(new ValidationException("Invalid IIN checksum."))
                .when(validationService).validatePayerIinChecksum(eq(INVALID_IIN));

        assertThrows(ValidationException.class,
                () -> instructionService.getInstructionsByPayerIin(INVALID_IIN),
                "Очікувався збій через некоректну контрольну суму.");

        verify(instructionClient, never()).getInstructionsForIin(anyString());
    }

    @Test
    void getInstructionsByRecipientEdrpou_shouldPassValidationAndReturnInstructionsFromClient() {

        List<Instruction> expectedList = List.of(createValidInstruction());

        doNothing().when(validationService).validatePayerEdrpouChecksum(eq(VALID_EDRPOU));

        when(instructionClient.getInstructionsForEdrpou(eq(VALID_EDRPOU)))
                .thenReturn(expectedList);

        List<Instruction> result = instructionService.getInstructionsByRecipientEdrpou(VALID_EDRPOU);

        assertFalse(result.isEmpty());
        assertEquals(expectedList.size(), result.size());

        verify(validationService, times(1)).validatePayerEdrpouChecksum(eq(VALID_EDRPOU));

        verify(instructionClient, times(1)).getInstructionsForEdrpou(eq(VALID_EDRPOU));
    }

    @Test
    void shouldPassValidationAndReturnEmptyList() {

        List<Instruction> expectedList = Collections.emptyList();

        doNothing().when(validationService).validatePayerEdrpouChecksum(eq(VALID_EDRPOU));

        when(instructionClient.getInstructionsForEdrpou(eq(VALID_EDRPOU)))
                .thenReturn(expectedList);

        List<Instruction> result = instructionService.getInstructionsByRecipientEdrpou(VALID_EDRPOU);

        assertTrue(result.isEmpty());
        verify(validationService, times(1)).validatePayerEdrpouChecksum(eq(VALID_EDRPOU));
        verify(instructionClient, times(1)).getInstructionsForEdrpou(eq(VALID_EDRPOU));
    }

    @Test
    void shouldThrowValidationExceptionAndNotCallClientWhenChecksumIsInvalid() {

        String invalidEdrpou = "99999999";

        doThrow(new ValidationException("Invalid EDRPOU checksum."))
                .when(validationService).validatePayerEdrpouChecksum(eq(invalidEdrpou));

        assertThrows(ValidationException.class,
                () -> instructionService.getInstructionsByRecipientEdrpou(invalidEdrpou),
                "Очікувався збій через некоректну контрольну суму.");

        verify(instructionClient, never()).getInstructionsForEdrpou(anyString());
    }

    private InstructionValidDTO createValidInstructionDTO() {
        return new InstructionValidDTO(
                "Іван", "Іваненко", "Іванович",
                "1234567890",
                "1111222233334444",
                "UA293123456789012345678901234",
                "320649",
                "40087654",
                "ТОВ Отримувач",
                new BigDecimal("100.00"),
                1,
                ChronoUnit.MONTHS
        );
    }

    private Instruction createValidInstruction() {
        Instruction mockInstruction = new Instruction();
        mockInstruction.setId(GENERATED_ID);
        mockInstruction.setPayerIin(VALID_IIN);
        mockInstruction.setRecipientEdrpou(VALID_EDRPOU);

        return mockInstruction;
    }
}

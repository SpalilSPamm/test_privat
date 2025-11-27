package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.TransactionClient;
import com.test.payment_pbls.dtos.BatchResultDTO;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.Transaction;
import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.utils.enums.InstructionStatus;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private Clock clock;

    @Mock
    private TransactionClient transactionClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;


    private static final Instant FIXED_INSTANT = Instant.parse("2025-11-26T10:00:00Z");
    private static final ZoneOffset TIME_ZONE = ZoneOffset.ofHours(2);

    private static final Long INSTRUCTION_ID = 10L;
    private static final Long TRANSACTION_ID = 50L;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100.00");

    @Test
    void createTransaction_shouldSetCriticalFieldsAndCallClient() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        when(transactionClient.createTransaction(any(Transaction.class))).thenReturn(createMockTransactionDTO());

        TransactionDTO result = transactionService.createTransaction(createMockInstruction());

        verify(transactionClient, times(1)).createTransaction(any(Transaction.class));

        assertNotNull(result);
        assertNotNull(result.idempotencyId());
        assertEquals(TRANSACTION_ID, result.id());
        assertEquals(INSTRUCTION_ID, result.instructionId());
        assertEquals(TransactionStatus.ACTIVE.getStatusCode(), result.transactionStatus());
        assertEquals(TEST_AMOUNT, result.amount());
    }

    @Test
    void createTransaction_shouldHandleClientFailure() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        doThrow(new CreationFailureException("PDS error"))
                .when(transactionClient).createTransaction(any(Transaction.class));

        assertThrows(CreationFailureException.class, () -> transactionService.createTransaction(createMockInstruction()));
    }

    @Test
    void revertTransaction_shouldCallClientDeleteMethod() {

        Long revertId = 123L;

        doNothing().when(transactionClient).revertTransaction(eq(revertId));

        assertDoesNotThrow(() -> transactionService.revertTransaction(revertId));

        verify(transactionClient, times(1)).revertTransaction(eq(revertId));
    }

    @Test
    void revertTransaction_shouldHandleClientFailure() {

        Long revertId = 123L;

        doThrow(new CreationFailureException("Revert failed"))
                .when(transactionClient).revertTransaction(eq(revertId));

        assertThrows(CreationFailureException.class,
                () -> transactionService.revertTransaction(revertId));
    }

    @Test
    void getInstructionHistory_shouldReturnHistoryFromClient() {

        Long targetInstructionId = 100L;

        List<TransactionDTO> expectedHistory = List.of(createMockTransactionDTO());

        when(transactionClient.getTransactionsByInstructionId(eq(targetInstructionId)))
                .thenReturn(expectedHistory);

        List<TransactionDTO> result = transactionService.getInstructionHistory(targetInstructionId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(INSTRUCTION_ID, result.getFirst().instructionId());

        verify(transactionClient, times(1)).getTransactionsByInstructionId(eq(targetInstructionId));
    }

    @Test
    void getInstructionHistory_shouldReturnEmptyListIfNoHistory() {

        Long targetInstructionId = 100L;
        List<TransactionDTO> expectedHistory = Collections.emptyList();

        when(transactionClient.getTransactionsByInstructionId(eq(targetInstructionId)))
                .thenReturn(expectedHistory);

        List<TransactionDTO> result = transactionService.getInstructionHistory(targetInstructionId);

        assertTrue(result.isEmpty());
        verify(transactionClient, times(1)).getTransactionsByInstructionId(eq(targetInstructionId));
    }

    @Test
    void getInstructionHistory_shouldHandleClientFailure() {

        Long transactionId = 123L;

        doThrow(new CreationFailureException("Client failed"))
                .when(transactionClient).getTransactionsByInstructionId(eq(transactionId));

        assertThrows(CreationFailureException.class,
                () -> transactionService.getInstructionHistory(transactionId));
    }

    @Test
    void processBatch_ShouldReturnAllSuccess_WhenClientSucceeds() {

        when(clock.instant()).thenReturn(Instant.parse("2025-11-27T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Instruction instr1 = createMockInstruction();
        Instruction instr2 = createMockInstruction();
        List<Instruction> instructions = List.of(instr1, instr2);

        when(transactionClient.createTransaction(any(Transaction.class)))
                .thenReturn(new TransactionDTO(1L, 1L, "idempotency", BigDecimal.TEN, null, "A"));

        BatchResultDTO result = transactionService.processBatch(instructions);

        assertEquals(2, result.successCount());
        assertEquals(0, result.failureCount());
        assertTrue(result.failedInstructionIds().isEmpty());

        verify(transactionClient, times(2)).createTransaction(any(Transaction.class));
    }

    @Test
    void processBatch_ShouldHandleFailures_WhenClientThrowsException() {

        when(clock.instant()).thenReturn(Instant.parse("2025-11-27T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Instruction instr1 = createMockInstruction();
        instr1.setId(1L);
        Instruction instr2 = createMockInstruction();
        instr2.setId(20L);

        List<Instruction> instructions = List.of(instr1, instr2);

        when(transactionClient.createTransaction(any(Transaction.class)))
                .thenReturn(new TransactionDTO(1L, 10L, "key", BigDecimal.TEN, null, "A"))
                .thenThrow(new CreationFailureException("PDS Error"));

        BatchResultDTO result = transactionService.processBatch(instructions);

        assertEquals(1, result.successCount());
        assertEquals(1, result.failureCount());

        assertEquals(1, result.failedInstructionIds().size());
        assertEquals(20L, result.failedInstructionIds().getFirst());

        verify(transactionClient, times(2)).createTransaction(any(Transaction.class));
    }

    @Test
    void processBatch_ShouldReturnAllFailures_WhenAllFail() {

        when(clock.instant()).thenReturn(Instant.parse("2025-11-27T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        List<Instruction> instructions = List.of(createMockInstruction(), createMockInstruction());

        when(transactionClient.createTransaction(any(Transaction.class)))
                .thenThrow(new RuntimeException("Database down"));

        BatchResultDTO result = transactionService.processBatch(instructions);

        assertEquals(0, result.successCount());
        assertEquals(2, result.failureCount());
        assertEquals(2, result.failedInstructionIds().size());
    }

    @Test
    void processBatch_ShouldHandleEmptyList() {

        List<Instruction> instructions = Collections.emptyList();

        BatchResultDTO result = transactionService.processBatch(instructions);

        assertEquals(0, result.successCount());
        assertEquals(0, result.failureCount());
        assertTrue(result.failedInstructionIds().isEmpty());

        verifyNoInteractions(transactionClient);
    }

    private TransactionDTO createMockTransactionDTO() {
        return new TransactionDTO(
                TRANSACTION_ID,
                INSTRUCTION_ID,
                UUID.randomUUID().toString(),
                TEST_AMOUNT,
                OffsetDateTime.now(),
                TransactionStatus.ACTIVE.getStatusCode()
        );
    }

    private Instruction createMockInstruction() {
        Instruction instruction = new Instruction();
        instruction.setId(INSTRUCTION_ID);
        instruction.setPayerIin("1111111118");
        instruction.setRecipientEdrpou("40087654");
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);
        instruction.setAmount(TEST_AMOUNT);
        instruction.setPeriodUnit(ChronoUnit.HOURS);
        instruction.setPeriodValue(1);
        return instruction;
    }
}

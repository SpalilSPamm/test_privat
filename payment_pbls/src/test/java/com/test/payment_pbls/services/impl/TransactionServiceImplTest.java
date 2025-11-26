package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.TransactionClient;
import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.models.Transaction;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

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
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("50.00");
    private Instruction mockInstruction;

    @BeforeEach
    void setUp() {

        mockInstruction = new Instruction();
        mockInstruction.setId(INSTRUCTION_ID);
        mockInstruction.setAmount(TEST_AMOUNT);
    }

    @Test
    void createTransaction_shouldSetCriticalFieldsAndCallClient() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        OffsetDateTime expectedTime = OffsetDateTime.ofInstant(FIXED_INSTANT, TIME_ZONE);

        when(transactionClient.createTransaction(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction arg = invocation.getArgument(0);
                    arg.setId(TRANSACTION_ID);
                    return arg;
                });

        Transaction result = transactionService.createTransaction(mockInstruction);

        verify(transactionClient, times(1)).createTransaction(any(Transaction.class));

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getId());
        assertEquals(INSTRUCTION_ID, result.getInstruction().getId());
        assertEquals(TransactionStatus.ACTIVE.getStatusCode(), result.getTransactionStatus());
        assertEquals(TEST_AMOUNT, result.getAmount());

        assertNotNull(result.getIdempotencyId());
        assertEquals(expectedTime, result.getTransactionTime());
    }

    @Test
    void createTransaction_shouldHandleClientFailure() {

        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(TIME_ZONE);

        doThrow(new CreationFailureException("PDS error"))
                .when(transactionClient).createTransaction(any(Transaction.class));

        assertThrows(CreationFailureException.class, () -> transactionService.createTransaction(mockInstruction));
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

        Transaction transaction = new Transaction();
        transaction.setInstruction(mockInstruction);

        List<Transaction> expectedHistory = List.of(transaction);

        when(transactionClient.getTransactionsByInstructionId(eq(targetInstructionId)))
                .thenReturn(expectedHistory);

        List<Transaction> result = transactionService.getInstructionHistory(targetInstructionId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(INSTRUCTION_ID, result.getFirst().getInstruction().getId());

        verify(transactionClient, times(1)).getTransactionsByInstructionId(eq(targetInstructionId));
    }

    @Test
    void getInstructionHistory_shouldReturnEmptyListIfNoHistory() {

        Long targetInstructionId = 100L;
        List<Transaction> expectedHistory = Collections.emptyList();

        when(transactionClient.getTransactionsByInstructionId(eq(targetInstructionId)))
                .thenReturn(expectedHistory);

        List<Transaction> result = transactionService.getInstructionHistory(targetInstructionId);

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
}

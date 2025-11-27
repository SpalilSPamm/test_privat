package com.test.payment_jar.services.impl;

import com.test.payment_jar.clients.BusinessLogicClient;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegularPaymentServiceImplTest {

    @Mock
    private BusinessLogicClient businessLogicClient;

    @InjectMocks
    private RegularPaymentServiceImpl regularPaymentService;

    private Instruction instruction1;
    private Instruction instruction2;

    @BeforeEach
    void setUp() {
        instruction1 = createInstruction(1L, "100.00");
        instruction2 = createInstruction(2L, "250.50");
    }

    @Test
    void processPayments_shouldCreateTransactionForEachInstruction() {

        List<Instruction> instructions = List.of(instruction1, instruction2);
        when(businessLogicClient.getAllActiveInstructions()).thenReturn(instructions);

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(1)).getAllActiveInstructions();
        verify(businessLogicClient, times(1)).createTransaction(instruction1);
        verify(businessLogicClient, times(1)).createTransaction(instruction2);
    }

    @Test
    void processPayments_shouldNotCreateTransactionWhenNoInstructions() {

        when(businessLogicClient.getAllActiveInstructions()).thenReturn(Collections.emptyList());

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(1)).getAllActiveInstructions();

        verify(businessLogicClient, never()).createTransaction(any(Instruction.class));
    }

    @Test
    void processPayments_shouldContinueAfterCreationFailure() {

        List<Instruction> instructions = List.of(instruction1, instruction2);
        when(businessLogicClient.getAllActiveInstructions()).thenReturn(instructions);

        doThrow(new CreationFailureException("Balance check failed"))
                .when(businessLogicClient).createTransaction(instruction1);

        doNothing().when(businessLogicClient).createTransaction(instruction2);

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(1)).createTransaction(instruction1);
        verify(businessLogicClient, times(1)).createTransaction(instruction2);

        verify(businessLogicClient, times(1)).getAllActiveInstructions();
    }

    private Instruction createInstruction(Long id, String amount) {
        Instruction instruction = new Instruction();
        instruction.setId(id);
        instruction.setAmount(new BigDecimal(amount));
        return instruction;
    }
}
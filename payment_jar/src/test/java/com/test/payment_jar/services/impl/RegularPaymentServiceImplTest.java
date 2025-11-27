package com.test.payment_jar.services.impl;

import com.test.payment_jar.clients.BusinessLogicClient;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegularPaymentServiceImplTest {

    @Mock
    private BusinessLogicClient businessLogicClient;

    @InjectMocks
    private RegularPaymentServiceImpl regularPaymentService;

    private static final int PAGE_SIZE = 1000;

    @Test
    void processPayments_ShouldDoNothing_WhenNoInstructionsReturned() {

        when(businessLogicClient.getScheduledInstructions(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(1)).getScheduledInstructions(eq(0), eq(PAGE_SIZE));
        verify(businessLogicClient, never()).createTransactionsBatch(anyList());
    }

    @Test
    void processPayments_ShouldProcessOneBatch_WhenResultIsLessThanPageSize() {

        List<Instruction> smallBatch = createMockInstructions(5);

        when(businessLogicClient.getScheduledInstructions(eq(0), eq(PAGE_SIZE)))
                .thenReturn(smallBatch);

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(1)).getScheduledInstructions(eq(0), eq(PAGE_SIZE));

        verify(businessLogicClient, times(1)).createTransactionsBatch(smallBatch);
    }

    @Test
    void processPayments_ShouldProcessMultipleBatches_WhenFirstBatchIsFull() {

        List<Instruction> fullBatch = createMockInstructions(PAGE_SIZE);

        List<Instruction> lastBatch = createMockInstructions(50);

        when(businessLogicClient.getScheduledInstructions(eq(0), eq(PAGE_SIZE)))
                .thenReturn(fullBatch)
                .thenReturn(lastBatch);

        regularPaymentService.processPayments();

        InOrder inOrder = inOrder(businessLogicClient);

        inOrder.verify(businessLogicClient).getScheduledInstructions(0, PAGE_SIZE);
        inOrder.verify(businessLogicClient).createTransactionsBatch(fullBatch);

        inOrder.verify(businessLogicClient).getScheduledInstructions(0, PAGE_SIZE);
        inOrder.verify(businessLogicClient).createTransactionsBatch(lastBatch);

        verify(businessLogicClient, times(2)).getScheduledInstructions(anyInt(), anyInt());
    }

    @Test
    void processPayments_ShouldStop_WhenBatchIsFullButNextIsEmpty() {

        List<Instruction> fullBatch = createMockInstructions(PAGE_SIZE);
        List<Instruction> emptyBatch = Collections.emptyList();

        when(businessLogicClient.getScheduledInstructions(eq(0), eq(PAGE_SIZE)))
                .thenReturn(fullBatch)
                .thenReturn(emptyBatch);

        regularPaymentService.processPayments();

        verify(businessLogicClient, times(2)).getScheduledInstructions(0, PAGE_SIZE);

        verify(businessLogicClient, times(1)).createTransactionsBatch(anyList());
        verify(businessLogicClient).createTransactionsBatch(fullBatch);
    }

    @Test
    void processPayments_ShouldPropagateException_WhenClientFails() {

        when(businessLogicClient.getScheduledInstructions(anyInt(), anyInt()))
                .thenThrow(new CreationFailureException("Service Unavailable"));


        assertThrows(CreationFailureException.class, () -> regularPaymentService.processPayments());
    }

    private List<Instruction> createMockInstructions(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Instruction instruction = new Instruction();
                    instruction.setId((long) i);
                    return instruction;
                })
                .collect(Collectors.toList());
    }
}
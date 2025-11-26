package com.test.payment_pbls.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.models.Transaction;
import com.test.payment_pbls.services.TransactionService;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import com.test.payment_pbls.utils.exceptions.TransactionNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Long INSTRUCTION_ID = 10L;
    private static final Long TRANSACTION_ID = 50L;

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {

        Instruction inputInstruction = createMockInstruction();
        Transaction mockSavedTransaction = createMockTransaction();

        when(transactionService.createTransaction(any(Instruction.class)))
                .thenReturn(mockSavedTransaction);

        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.transactionStatus", is("A"))); // 'A' з TransactionStatus.ACTIVE
    }

    @Test
    void shouldReturn400BadRequestWhenCreationFails() throws Exception {

        Instruction inputInstruction = createMockInstruction();

        when(transactionService.createTransaction(any(Instruction.class)))
                .thenThrow(new CreationFailureException(""));

        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRevertTransactionAndReturn200() throws Exception {

        Long revertId = 123L;

        doNothing().when(transactionService).revertTransaction(eq(revertId));

        mockMvc.perform(patch("/transactions/revert/{transactionId}", revertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void shouldReturn404NotFoundWhenRevertingNonExistentTransaction() throws Exception {

        Long nonExistentId = 999L;

        doThrow(new TransactionNotFoundException("Transaction not found for storno."))
                .when(transactionService).revertTransaction(eq(nonExistentId));

        mockMvc.perform(patch("/transactions/revert/{transactionId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnTransactionHistoryAnd200() throws Exception {

        List<Transaction> historyList = List.of(createMockTransaction(), createMockTransaction());

        when(transactionService.getInstructionHistory(eq(INSTRUCTION_ID)))
                .thenReturn(historyList);

        mockMvc.perform(get("/transactions/{instructionId}/history", INSTRUCTION_ID))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(TRANSACTION_ID.intValue())));
    }

    @Test
    void shouldReturnEmptyListHistoryAnd200() throws Exception {

        when(transactionService.getInstructionHistory(eq(INSTRUCTION_ID)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions/{instructionId}/history", INSTRUCTION_ID))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Instruction createMockInstruction() {
        Instruction instruction = new Instruction();
        instruction.setId(INSTRUCTION_ID);
        instruction.setAmount(new BigDecimal("100.00"));

        return instruction;
    }

    private Transaction createMockTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setInstruction(createMockInstruction());
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setTransactionTime(OffsetDateTime.now());
        return transaction;
    }
}

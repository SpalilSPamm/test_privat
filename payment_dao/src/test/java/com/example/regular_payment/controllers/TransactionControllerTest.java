package com.example.regular_payment.controllers;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.services.TransactionService;
import com.example.regular_payment.utils.enums.TransactionStatus;
import com.example.regular_payment.utils.exceptions.TransactionNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


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

    private static final Long TEST_INSTRUCTION_ID = 100L;
    private static final Long TRANSACTION_ID = 200L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final String IDEMPOTENCY_KEY = UUID.randomUUID().toString();

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {

        Transaction inputTransaction = createValidTransaction();

        Transaction expectedTransaction = createValidTransaction();
        expectedTransaction.setId(TRANSACTION_ID);

        when(transactionService.createTransaction(any(Transaction.class)))
                .thenReturn(expectedTransaction);

        String transactionJson = objectMapper.writeValueAsString(inputTransaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.amount", is(50.00)))
                .andExpect(jsonPath("$.transactionStatus", is("A")));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldIsMissing() throws Exception {

        Transaction transaction = createValidTransaction();
        transaction.setAmount(null);

        String instructionJson = objectMapper.writeValueAsString(transaction);

        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenIdempotencyKeyIsDuplicate() throws Exception {

        Transaction inputTransaction = createValidTransaction();
        String transactionJson = objectMapper.writeValueAsString(inputTransaction);

        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate Idempotency id detected."));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Duplicate Idempotency id detected.")));
    }

    @Test
    void shouldUpdateTransactionStatusAndReturn200() throws Exception {
        Transaction inputTransaction = createValidTransaction();
        inputTransaction.setId(TRANSACTION_ID);
        inputTransaction.setTransactionStatus(TransactionStatus.REVERSED.getStatusCode());

        Transaction expectedTransaction = createValidTransaction();
        expectedTransaction.setId(TRANSACTION_ID);
        expectedTransaction.setTransactionStatus(TransactionStatus.REVERSED.getStatusCode());

        when(transactionService.updateTransaction(eq(TRANSACTION_ID), any(Transaction.class)))
                .thenReturn(expectedTransaction);

        String transactionJson = objectMapper.writeValueAsString(inputTransaction);

        mockMvc.perform(put("/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.transactionStatus", is("S")));
    }

    @Test
    void shouldReturn404NotFoundWhenTransactionDoesNotExistWhenUpdating() throws Exception {

        Transaction inputTransaction = createValidTransaction();
        String transactionJson = objectMapper.writeValueAsString(inputTransaction);

        when(transactionService.updateTransaction(eq(NON_EXISTENT_ID), any(Transaction.class)))
                .thenThrow(new TransactionNotFoundException("Transaction with ID " + NON_EXISTENT_ID + " not found."));

        mockMvc.perform(put("/transactions/{id}", NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Transaction with ID " + NON_EXISTENT_ID + " not found.")));
    }

    @Test
    void shouldReturn400BadRequestWhenUpdatingWithInvalidData() throws Exception {

        Transaction inputTransaction = createValidTransaction();
        inputTransaction.setId(TRANSACTION_ID);
        inputTransaction.setAmount(null);
        String instructionJson = objectMapper.writeValueAsString(inputTransaction);

        when(transactionService.updateTransaction(eq(TRANSACTION_ID), any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(put("/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteTransactionAndReturn200() throws Exception {

        doNothing().when(transactionService).deleteTransaction(eq(TRANSACTION_ID));

        mockMvc.perform(delete("/transactions/{id}", TRANSACTION_ID)) // Припускаємо, що URL контролера /transactions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void shouldReturn404NotFoundWhenTransactionDoesNotExistWhenDeleting() throws Exception {

        doThrow(new TransactionNotFoundException("Transaction with ID " + NON_EXISTENT_ID + " not found."))
                .when(transactionService).deleteTransaction(eq(NON_EXISTENT_ID));

        mockMvc.perform(delete("/transactions/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Transaction with ID " + NON_EXISTENT_ID + " not found.")));
    }

    @Test
    void shouldReturnTransactionAnd200() throws Exception {

        Transaction expectedTransaction = createValidTransaction();
        expectedTransaction.setId(TRANSACTION_ID);
        when(transactionService.getTransaction(eq(TRANSACTION_ID)))
                .thenReturn(expectedTransaction);

        mockMvc.perform(get("/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.amount", is(50.00)))
                .andExpect(jsonPath("$.transactionStatus", is("A")));
    }

    @Test
    void shouldReturn404NotFound() throws Exception {

        when(transactionService.getTransaction(eq(NON_EXISTENT_ID)))
                .thenThrow(new TransactionNotFoundException("Transaction with ID " + NON_EXISTENT_ID + " not found."));

        mockMvc.perform(get("/transactions/{id}", NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Transaction with ID " + NON_EXISTENT_ID + " not found.")));
    }

    @Test
    void shouldReturnTransactionListAnd200WhenHistoryExists() throws Exception {

        List<Transaction> expectedList = createTransactionList();
        when(transactionService.getTransactionsByInstruction(eq(TEST_INSTRUCTION_ID)))
                .thenReturn(expectedList);

        mockMvc.perform(get("/transactions/instruction/{instructionId}", TEST_INSTRUCTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(200)))
                .andExpect(jsonPath("$[0].transactionStatus", is("A")))
                .andExpect(jsonPath("$[1].id", is(201)))
                .andExpect(jsonPath("$[1].transactionStatus", is("S")));
    }

    @Test
    void shouldReturnEmptyListAnd200WhenNoHistoryExists() throws Exception {

        when(transactionService.getTransactionsByInstruction(eq(45L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions/instruction/{instructionId}", 45L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Transaction createValidTransaction() {

        Instruction instruction = new Instruction();
        instruction.setId(TEST_INSTRUCTION_ID);

        Transaction transaction = new Transaction();

        transaction.setInstruction(instruction);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setTransactionTime(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setIdempotencyId(IDEMPOTENCY_KEY);

        return transaction;
    }

    private List<Transaction> createTransactionList() {

        Instruction instruction = new Instruction();
        instruction.setId(TEST_INSTRUCTION_ID);

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Transaction transaction = new Transaction();
            transaction.setId(200L + i);
            transaction.setInstruction(instruction);
            transaction.setAmount(new BigDecimal("50.00"));
            transaction.setTransactionStatus(i % 2 == 0 ? TransactionStatus.ACTIVE.getStatusCode() : TransactionStatus.REVERSED.getStatusCode());
            transaction.setIdempotencyId(UUID.randomUUID().toString());
            transactions.add(transaction);
        }
        return transactions;
    }
}

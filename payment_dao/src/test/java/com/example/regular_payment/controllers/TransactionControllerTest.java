package com.example.regular_payment.controllers;

import com.example.regular_payment.dtos.TransactionCreateDTO;
import com.example.regular_payment.dtos.TransactionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.services.TransactionService;
import com.example.regular_payment.utils.enums.TransactionStatus;
import com.example.regular_payment.utils.exceptions.TransactionNotFoundException;
import com.example.regular_payment.utils.mappers.TransactionMapper;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private TransactionMapper transactionMapper;

    @MockitoBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Long TEST_INSTRUCTION_ID = 100L;
    private static final Long TRANSACTION_ID = 200L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final String IDEMPOTENCY_KEY = UUID.randomUUID().toString();



    private TransactionCreateDTO createValidTransactionCreateDTO() {

        Instruction instruction = new Instruction();
        instruction.setId(TEST_INSTRUCTION_ID);

        return new TransactionCreateDTO(
                instruction,
                UUID.randomUUID().toString(),
                new BigDecimal("50.00"),
                OffsetDateTime.ofInstant(Instant.parse("2026-11-29T10:00:00Z"), ZoneOffset.ofHours(2)).truncatedTo(ChronoUnit.SECONDS),
                "A"
        );
    }

    private TransactionDTO createMockTransactionDTO() {
        // Створює DTO, яка імітує об'єкт, що повертається клієнту
        return new TransactionDTO(
                TRANSACTION_ID,
                TEST_INSTRUCTION_ID,
                IDEMPOTENCY_KEY,
                new BigDecimal("50.00"),
                OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                "A"
        );
    }

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {

        TransactionCreateDTO dto = createValidTransactionCreateDTO();
        Transaction unsavedEntity = createValidTransaction();
        Transaction savedEntity = createValidTransaction();
        TransactionDTO responseDTO = createMockTransactionDTO();

        when(transactionMapper.toEntity(any(TransactionCreateDTO.class)))
                .thenReturn(unsavedEntity);

        when(transactionService.createTransaction(eq(unsavedEntity)))
                .thenReturn(savedEntity);

        when(transactionMapper.toDTO(eq(savedEntity)))
                .thenReturn(responseDTO);

        String dtoJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.amount", is(50.00)))
                .andExpect(jsonPath("$.instructionId", is(TEST_INSTRUCTION_ID.intValue()))) // Ключовий DTO-асерт
                .andExpect(jsonPath("$.transactionStatus", is("A")));

        verify(transactionMapper, times(1)).toDTO(eq(savedEntity));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldIsMissing() throws Exception {

        TransactionCreateDTO dto = createValidTransactionCreateDTO();
        Transaction unsavedEntity = createValidTransaction();

        when(transactionMapper.toEntity(any(TransactionCreateDTO.class)))
                .thenReturn(unsavedEntity);

        String instructionJson = objectMapper.writeValueAsString(dto);

        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsDuplicate() throws Exception {

        TransactionCreateDTO dto = createValidTransactionCreateDTO();

        when(transactionMapper.toEntity(any(TransactionCreateDTO.class)))
                .thenReturn(createValidTransaction());

        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate Idempotency id detected."));

        String dtoJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Duplicate Idempotency id detected.")));
    }

    @Test
    void shouldUpdateTransactionStatusAndReturn200() throws Exception {

        TransactionDTO inputDTO = createMockTransactionDTO();
        Transaction unsavedEntity = createValidTransaction();
        Transaction savedEntity = createValidTransaction();
        savedEntity.setTransactionStatus(TransactionStatus.REVERSED.getStatusCode());
        TransactionDTO responseDTO = createMockTransactionDTO();
        responseDTO = new TransactionDTO(responseDTO.id(), responseDTO.instructionId(), responseDTO.idempotencyId(),
                responseDTO.amount(), responseDTO.transactionTime(), TransactionStatus.REVERSED.getStatusCode());

        when(transactionMapper.toEntity(any(TransactionDTO.class)))
                .thenReturn(unsavedEntity);

        when(transactionService.updateTransaction(eq(TRANSACTION_ID), eq(unsavedEntity)))
                .thenReturn(savedEntity);

        when(transactionMapper.toDTO(eq(savedEntity)))
                .thenReturn(responseDTO);

        String dtoJson = objectMapper.writeValueAsString(inputDTO);

        mockMvc.perform(put("/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.transactionStatus", is("S"))); // Перевіряємо REVERSED
    }

    @Test
    void shouldReturn404NotFoundWhenTransactionDoesNotExistWhenUpdating() throws Exception {

        TransactionDTO dto = createMockTransactionDTO();

        when(transactionMapper.toEntity(any(TransactionDTO.class)))
                .thenReturn(createValidTransaction());

        String transactionJson = objectMapper.writeValueAsString(dto);

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

        TransactionDTO validDTO = createMockTransactionDTO();
        TransactionDTO invalidInputDTO = new TransactionDTO(
                validDTO.id(), validDTO.instructionId(), validDTO.idempotencyId(),
                null,
                validDTO.transactionTime(), validDTO.transactionStatus()
        );

        Transaction entityWithNullAmount = createValidTransaction();
        entityWithNullAmount.setAmount(null);

        when(transactionMapper.toEntity(any(TransactionDTO.class)))
                .thenReturn(entityWithNullAmount);

        when(transactionService.updateTransaction(eq(TRANSACTION_ID), eq(entityWithNullAmount)))
                .thenThrow(new DataIntegrityViolationException(""));

        String dtoJson = objectMapper.writeValueAsString(invalidInputDTO);

        mockMvc.perform(put("/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
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

        when(transactionMapper.toDTO(eq(expectedTransaction))).thenReturn(createMockTransactionDTO());

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

        List<Transaction> expectedEntities = List.of(createValidTransaction(), createValidTransaction());

        when(transactionService.getTransactionsByInstruction(eq(TEST_INSTRUCTION_ID)))
                .thenReturn(expectedEntities);

        when(transactionMapper.toDTO(any(Transaction.class)))
                .thenReturn(createMockTransactionDTO());

        mockMvc.perform(get("/transactions/instruction/{instructionId}", TEST_INSTRUCTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$[0].instructionId", is(TEST_INSTRUCTION_ID.intValue())));

        verify(transactionMapper, times(2)).toDTO(any(Transaction.class));
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
}

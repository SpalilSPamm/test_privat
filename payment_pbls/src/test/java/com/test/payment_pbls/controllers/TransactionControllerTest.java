package com.test.payment_pbls.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.payment_pbls.dtos.BatchResultDTO;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.services.TransactionService;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import com.test.payment_pbls.utils.exceptions.TransactionNotFoundException;
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
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Long INSTRUCTION_ID = 10L;
    private static final Long TRANSACTION_ID = 50L;

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {

        Instruction inputInstruction = createMockInstruction();
        TransactionDTO mockSavedTransaction = createMockTransactionDTO();

        when(transactionService.createTransaction(any(Instruction.class)))
                .thenReturn(mockSavedTransaction);

        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(TRANSACTION_ID.intValue())))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.transactionStatus", is("A")));
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

        List<TransactionDTO> historyList = List.of(createMockTransactionDTO(), createMockTransactionDTO());

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

    @Test
    void createTransactionsBatch_ShouldReturnOkAndStats_WhenCalledWithList() throws Exception {

        Instruction instruction1 = new Instruction();
        instruction1.setId(101L);
        Instruction instruction2 = new Instruction();
        instruction2.setId(102L);

        List<Instruction> inputList = List.of(instruction1, instruction2);

        BatchResultDTO mockResult = new BatchResultDTO(1, 1, List.of(102L));

        when(transactionService.processBatch(anyList())).thenReturn(mockResult);

        mockMvc.perform(post("/transactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.failedInstructionIds[0]").value(102));

        verify(transactionService).processBatch(anyList());
    }

    @Test
    void createTransactionsBatch_ShouldHandleEmptyList() throws Exception {

        BatchResultDTO emptyResult = new BatchResultDTO(0, 0, Collections.emptyList());

        when(transactionService.processBatch(anyList())).thenReturn(emptyResult);

        mockMvc.perform(post("/transactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0));
    }

    private Instruction createMockInstruction() {
        Instruction instruction = new Instruction();
        instruction.setId(INSTRUCTION_ID);
        instruction.setAmount(new BigDecimal("100.00"));

        return instruction;
    }

    private TransactionDTO createMockTransactionDTO() {
      return new TransactionDTO(
              TRANSACTION_ID,
              INSTRUCTION_ID,
              UUID.randomUUID().toString(),
              new BigDecimal("100.00"),
              OffsetDateTime.now(),
              TransactionStatus.ACTIVE.getStatusCode()
      );
    }
}

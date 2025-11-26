package com.example.regular_payment.controllers;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@WebMvcTest(InstructionController.class)
public class InstructionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstructionService instructionService;


    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @Test
    void shouldCreateInstructionAndReturn201() throws Exception {

        Instruction newInstruction = createValidInstruction();

        Instruction expectedInstruction = createValidInstruction();
        expectedInstruction.setId(100L);

        when(instructionService.saveInstruction(any(Instruction.class)))
                .thenReturn(expectedInstruction);

        String instructionJson = objectMapper.writeValueAsString(newInstruction);

        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.payerIin", is(newInstruction.getPayerIin())))
                .andExpect(jsonPath("$.amount", is(100.50)))
                .andExpect(jsonPath("$.instructionStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.nextExecutionAt").exists())
                .andExpect(jsonPath("$.lastExecutionAt").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldIsMissing() throws Exception {

        Instruction invalidInstruction = createValidInstruction();
        invalidInstruction.setPayerIin(null);

        String instructionJson = objectMapper.writeValueAsString(invalidInstruction);

        when(instructionService.saveInstruction(any(Instruction.class)))
                .thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateInstructionAndReturn200() throws Exception {

        Long testId = 42L;

        Instruction inputInstruction = createValidInstruction();
        inputInstruction.setAmount(new BigDecimal("250.75"));
        inputInstruction.setPayerFirstName("Олег");
        inputInstruction.setId(testId);

        when(instructionService.updateInstruction(eq(testId), any(Instruction.class)))
                .thenReturn(inputInstruction);

        String instructionJson = objectMapper.writeValueAsString(inputInstruction);


        mockMvc.perform(put("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testId.intValue())))
                .andExpect(jsonPath("$.payerFirstName", is("Олег")))
                .andExpect(jsonPath("$.amount", is(250.75)))
                .andExpect(jsonPath("$.instructionStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.nextExecutionAt").exists());
    }

    @Test
    void shouldReturn404NotFoundWhenInstructionDoesNotExistWhenUpdating() throws Exception {

        Long testId = 42L;

        Instruction inputInstruction = createValidInstruction();
        inputInstruction.setId(testId);
        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        when(instructionService.updateInstruction(eq(testId), any(Instruction.class)))
                .thenThrow(new InstructionNotFoundException("Instruction with ID " + testId + " not found in PDS."));

        mockMvc.perform(put("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Instruction with ID 42 not found in PDS.")));
    }

    @Test
    void shouldReturn400BadRequestWhenUpdatingWithInvalidData() throws Exception {

        Long testId = 42L;

        Instruction inputInstruction = createValidInstruction();
        inputInstruction.setId(testId);
        inputInstruction.setPayerIin(null);
        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        when(instructionService.updateInstruction(eq(testId), any(Instruction.class)))
                .thenThrow(new DataIntegrityViolationException(""));

        mockMvc.perform(put("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instructionJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteInstructionAndReturn200() throws Exception {

        Long testId = 42L;

        doNothing().when(instructionService).deleteInstruction(eq(testId));

        mockMvc.perform(delete("/instructions/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void shouldReturn404NotFoundWhenInstructionDoesNotExistWhenDeleting() throws Exception {

        Long testId = 42L;

        doThrow(new InstructionNotFoundException("Instruction with ID " + testId + " not found in PDS."))
                .when(instructionService).deleteInstruction(eq( testId));

        mockMvc.perform(delete("/instructions/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Instruction with ID 42 not found in PDS.")));;
    }

    @Test
    void shouldReturnInstructionAnd200() throws Exception {

        Long testId = 42L;

        Instruction expectedInstruction = createValidInstruction();
        expectedInstruction.setId(testId);

        when(instructionService.getInstruction(eq(testId)))
                .thenReturn(expectedInstruction);

        mockMvc.perform(get("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testId.intValue())))
                .andExpect(jsonPath("$.payerIin", is(expectedInstruction.getPayerIin())))
                .andExpect(jsonPath("$.amount", is(100.50)))
                .andExpect(jsonPath("$.instructionStatus", is("ACTIVE")));
    }

    @Test
    void shouldReturn404NotFound() throws Exception {

        Long testId = 42L;

        when(instructionService.getInstruction(eq(testId)))
                .thenThrow(new InstructionNotFoundException("Instruction with ID " + testId + " not found in PDS."));

        mockMvc.perform(get("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Instruction with ID 42 not found in PDS.")));
    }

    @Test
    void shouldReturnInstructionListAnd200WhenResultsFound() throws Exception {

        List<Instruction> expectedList = createInstructionListWithTestIin(2);
        when(instructionService.getInstructionsByIin(eq("0123456789")))
                .thenReturn(expectedList);

        mockMvc.perform(get("/instructions/search/iin/{iin}", "0123456789")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].payerFirstName", is("Іван0")))
                .andExpect(jsonPath("$[0].payerIin", is("0123456789")))
                .andExpect(jsonPath("$[0].amount", is(50.00)))
                .andExpect(jsonPath("$[1].id", is(101)));
    }

    @Test
    void shouldReturnEmptyListAnd200WhenNoResultsFound() throws Exception {

        String nonExistentIin = "0000000000";

        when(instructionService.getInstructionsByIin(eq(nonExistentIin)))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/instructions/search/iin/{iin}", nonExistentIin)
                    .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnInstructionListAnd200WhenResultsFoundForSearchWithEdrpou() throws Exception {

        String testEdrpou = "44444444";

        List<Instruction> expectedList = createInstructionListWithTestEdrpou(3);
        when(instructionService.getInstructionsByEdrpou(eq(testEdrpou)))
                .thenReturn(expectedList);

        mockMvc.perform(get("/instructions/search/edrpou/{edrpou}", testEdrpou)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(200)))
                .andExpect(jsonPath("$[0].recipientEdrpou", is(testEdrpou)))
                .andExpect(jsonPath("$[0].amount", is(75.00)))
                .andExpect(jsonPath("$[2].id", is(202)));
    }

    @Test
    void shouldReturnEmptyListAnd200WhenNoResultsFoundForSearchWithEdrpou() throws Exception {

        String nonExistentEdrpou = "44444444";

        when(instructionService.getInstructionsByEdrpou(eq(nonExistentEdrpou)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/instructions/search/edrpou/{edrpou}", nonExistentEdrpou)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Instruction createValidInstruction() {
        Instruction instruction = new Instruction();

        instruction.setPayerFirstName("Іван");
        instruction.setPayerSecondName("Іваненко");
        instruction.setPayerPatronymic("Іванович");
        instruction.setPayerIin("1234567890"); // 10 цифр
        instruction.setPayerCardNumber("1111222233334444"); // 16 цифр

        instruction.setRecipientSettlementAccount("UA293123456789012345678901234");
        instruction.setRecipientBankCode("320649"); // МФО 6 цифр
        instruction.setRecipientEdrpou("40087654"); // 8 цифр
        instruction.setRecipientName("ТОВ Розрахункова Компанія");

        instruction.setAmount(new BigDecimal("100.50"));
        instruction.setPeriodValue(1);
        instruction.setPeriodUnit(ChronoUnit.MONTHS); // Використовуємо ChronoUnit

        instruction.setNextExecutionAt(OffsetDateTime.now().plusMonths(1).truncatedTo(ChronoUnit.SECONDS));

        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        instruction.setTransactions(List.of());

        return instruction;
    }

    private List<Instruction> createInstructionListWithTestIin(int count) {
        List<Instruction> instructions = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Instruction instruction = new Instruction();
            instruction.setId(100L + i);
            instruction.setPayerFirstName("Іван" + i);
            instruction.setPayerIin("0123456789");
            instruction.setAmount(new BigDecimal("50.00"));
            instruction.setInstructionStatus(InstructionStatus.ACTIVE);
            instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(i).truncatedTo(ChronoUnit.SECONDS));
            instructions.add(instruction);
        }
        return instructions;
    }

    private List<Instruction> createInstructionListWithTestEdrpou(int count) {
        List<Instruction> instructions = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Instruction instruction = new Instruction();
            instruction.setId(200L + i);
            instruction.setPayerFirstName("Одержувач" + i);
            instruction.setRecipientEdrpou("44444444");
            instruction.setAmount(new BigDecimal("75.00"));
            instruction.setInstructionStatus(InstructionStatus.ACTIVE);
            instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(i).truncatedTo(ChronoUnit.SECONDS));
            instructions.add(instruction);
        }
        return instructions;
    }
}

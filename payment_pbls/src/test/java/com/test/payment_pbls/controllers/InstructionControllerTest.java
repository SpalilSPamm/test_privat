package com.test.payment_pbls.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.payment_pbls.dtos.InstructionDTO;
import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.services.InstructionService;
import com.test.payment_pbls.utils.enums.InstructionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

    private static final String VALID_IIN = "1111111111";
    private static final String INVALID_IIN = "9999999999";
    private static final String VALID_EDRPOU = "40087654";
    private static final String INVALID_EDRPOU = "11111111";
    private static final Long GENERATED_ID = 500L;

    @Test
    void shouldCreateInstructionAndReturn201() throws Exception {
        // Arrange
        InstructionDTO dto = createValidInstructionDTO();
        Instruction mockSavedInstruction = createMockInstruction();

        // STUBBING: Сервіс повертає збережений об'єкт
        when(instructionService.createInstruction(any(InstructionDTO.class)))
                .thenReturn(mockSavedInstruction);

        String dtoJson = objectMapper.writeValueAsString(dto);

        // Act & Assert
        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))

                // 2. Перевірка статусу HTTP
                .andExpect(status().isCreated())

                // 3. Перевірка тіла відповіді (має бути об'єкт з ID)
                .andExpect(jsonPath("$.id", is(GENERATED_ID.intValue())))
                .andExpect(jsonPath("$.payerIin", is(VALID_IIN)));
    }

    @Test
    void shouldReturn400BadRequestWhenBusinessValidationFails() throws Exception {
        // Arrange
        InstructionDTO dto = createValidInstructionDTO();

        // STUBBING: Сервіс викидає помилку бізнес-валідації (наприклад, невірна контрольна сума)
        when(instructionService.createInstruction(any(InstructionDTO.class)))
                .thenThrow(new ValidationException("Invalid IIN checksum."));

        String dtoJson = objectMapper.writeValueAsString(dto);

        // Act & Assert
        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))

                // Ми припускаємо, що ControllerAdvice мапить ValidationException на 400 Bad Request
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenClientReturnException() throws Exception {

        InstructionDTO dto = createValidInstructionDTO();

        when(instructionService.createInstruction(any(InstructionDTO.class)))
                .thenThrow(new CreationFailureException("Invalid IIN checksum."));

        String dtoJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnInstructionListByIinAnd200() throws Exception {

        List<Instruction> expectedList = List.of(createMockInstruction());

        when(instructionService.getInstructionsByPayerIin(eq(VALID_IIN)))
                .thenReturn(expectedList);

        mockMvc.perform(get("/instructions/payer/{iin}", VALID_IIN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].payerIin", is(VALID_IIN)));
    }

    @Test
    void shouldReturn400WhenIinValidationFails_getInstructionsByPayerIin() throws Exception {

        when(instructionService.getInstructionsByPayerIin(eq(INVALID_IIN)))
                .thenThrow(new ValidationException("Invalid IIN checksum."));

        mockMvc.perform(get("/instructions/payer/{iin}", INVALID_IIN)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenClientReturnException_getInstructionsByPayerIin() throws Exception {

        when(instructionService.getInstructionsByPayerIin(eq(INVALID_IIN)))
                .thenThrow(new CreationFailureException(""));

        mockMvc.perform(get("/instructions/payer/{iin}", INVALID_IIN)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnInstructionListByEdrpouAnd200() throws Exception {

        List<Instruction> expectedList = List.of(createMockInstruction());

        when(instructionService.getInstructionsByRecipientEdrpou(eq(VALID_EDRPOU)))
                .thenReturn(expectedList);

        mockMvc.perform(get("/instructions/recipient/{edrpou}", VALID_EDRPOU)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientEdrpou", is(VALID_EDRPOU)));
    }


    @Test
    void shouldReturnEmptyListByEdrpouAnd200() throws Exception {

        when(instructionService.getInstructionsByRecipientEdrpou(eq(VALID_EDRPOU)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/instructions/recipient/{edrpou}", VALID_EDRPOU)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn400WhenIinValidationFails_getInstructionsByRecipientEdrpou() throws Exception {

        when(instructionService.getInstructionsByRecipientEdrpou(eq(INVALID_EDRPOU)))
                .thenThrow(new ValidationException("Invalid edrpou checksum."));

        mockMvc.perform(get("/instructions/recipient/{edrpou}", INVALID_EDRPOU)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenClientReturnException_getInstructionsByRecipientEdrpou() throws Exception {

        when(instructionService.getInstructionsByRecipientEdrpou(eq(INVALID_EDRPOU)))
                .thenThrow(new CreationFailureException(""));

        mockMvc.perform(get("/instructions/recipient/{edrpou}", INVALID_EDRPOU)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private InstructionDTO createValidInstructionDTO() {
        return new InstructionDTO(
                "Іван", "Іваненко", "Іванович",
                VALID_IIN, "1111222233334444",
                "UA293123456789012345678901234", "320649",
                VALID_EDRPOU, "ТОВ Отримувач",
                new BigDecimal("100.00"), 1, ChronoUnit.MONTHS
        );
    }


   // getInstructionsByRecipientEdrpou
    private Instruction createMockInstruction() {
        Instruction instruction = new Instruction();
        instruction.setId(GENERATED_ID);
        instruction.setPayerIin(VALID_IIN);
        instruction.setRecipientEdrpou(VALID_EDRPOU);
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);
        return instruction;
    }
}

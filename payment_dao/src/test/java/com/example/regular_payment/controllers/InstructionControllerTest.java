package com.example.regular_payment.controllers;

import com.example.regular_payment.dtos.InstructionCreateDTO;
import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;
import com.example.regular_payment.utils.mappers.InstructionMapper;
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
    private InstructionMapper instructionMapper;

    @MockitoBean
    private InstructionService instructionService;


    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldCreateInstructionAndReturn201() throws Exception {

        InstructionCreateDTO createDTO = createInstructionCreateDTO();
        Instruction mappedEntity = createInstructionEntity();
        Instruction savedEntity = createInstructionEntity();
        savedEntity.setId(100L);

        InstructionDTO responseDTO = createInstructionDTO(100L);

        when(instructionMapper.toEntity(any(InstructionCreateDTO.class))).thenReturn(mappedEntity);
        when(instructionService.saveInstruction(any(Instruction.class))).thenReturn(savedEntity);
        when(instructionMapper.toDTO(any(Instruction.class))).thenReturn(responseDTO);

        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.payerIin", is(createDTO.payerIin())))
                .andExpect(jsonPath("$.amount", is(500.50)))
                .andExpect(jsonPath("$.instructionStatus", is("ACTIVE")));
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsDataIntegrityException() throws Exception {

        InstructionCreateDTO createDTO = createInstructionCreateDTO();
        Instruction mappedEntity = createInstructionEntity();

        when(instructionMapper.toEntity(any(InstructionCreateDTO.class))).thenReturn(mappedEntity);

        when(instructionService.saveInstruction(any(Instruction.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        mockMvc.perform(post("/instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateInstructionAndReturn200() throws Exception {

        Long testId = 42L;
        InstructionDTO updateRequestDTO = createInstructionDTO(testId);

        Instruction updatedEntity = createInstructionEntity();
        updatedEntity.setId(testId);

        InstructionDTO responseDTO = createInstructionDTO(testId);

        when(instructionService.updateInstruction(eq(testId), any(InstructionDTO.class)))
                .thenReturn(updatedEntity);

        when(instructionMapper.toDTO(updatedEntity)).thenReturn(responseDTO);

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDTO);

        mockMvc.perform(put("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(42)))
                .andExpect(jsonPath("$.payerFirstName", is(updateRequestDTO.payerFirstName())))
                .andExpect(jsonPath("$.amount", is(500.50)));
    }

    @Test
    void shouldReturn404NotFoundWhenUpdatingNonExistentInstruction() throws Exception {
        Long testId = 42L;
        InstructionDTO updateRequestDTO = createInstructionDTO(testId);

        when(instructionService.updateInstruction(eq(testId), any(InstructionDTO.class)))
                .thenThrow(new InstructionNotFoundException("Instruction with ID " + testId + " not found"));

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDTO);

        mockMvc.perform(put("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenUpdatingWithInvalidData() throws Exception {

        Long testId = 42L;

        Instruction inputInstruction = createInstructionEntity();
        inputInstruction.setId(testId);
        inputInstruction.setPayerIin(null);
        String instructionJson = objectMapper.writeValueAsString(inputInstruction);

        when(instructionService.updateInstruction(eq(testId), any(InstructionDTO.class)))
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

        Instruction expectedInstruction = createInstructionEntity();
        expectedInstruction.setId(testId);

        when(instructionService.getInstruction(eq(testId)))
                .thenReturn(expectedInstruction);

        when(instructionMapper.toDTO(expectedInstruction)).thenReturn(createInstructionDTO(testId));

        mockMvc.perform(get("/instructions/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testId.intValue())))
                .andExpect(jsonPath("$.payerIin", is(expectedInstruction.getPayerIin())))
                .andExpect(jsonPath("$.amount", is(500.50)))
                .andExpect(jsonPath("$.instructionStatus", is("ACTIVE")));
    }

    @Test
    void shouldSearchByEdrpouAndReturnListOfDTOs() throws Exception {

        String edrpou = "12345678";
        Instruction entity1 = createInstructionEntity();
        entity1.setId(1L);
        Instruction entity2 = createInstructionEntity();
        entity2.setId(2L);

        List<Instruction> entities = List.of(entity1, entity2);

        when(instructionService.getInstructionsByEdrpou(edrpou)).thenReturn(entities);

        when(instructionMapper.toDTO(any(Instruction.class)))
                .thenReturn(createInstructionDTO(1L))
                .thenReturn(createInstructionDTO(2L));

        mockMvc.perform(get("/instructions/search/edrpou/{edrpou}", edrpou))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
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

        List<Instruction> expectedList = createInstructionListWithTestIin();
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
        int count = 3;

        List<Instruction> entities = createInstructionListWithTestEdrpou(count);

        when(instructionService.getInstructionsByEdrpou(eq(testEdrpou)))
                .thenReturn(entities);

        for (Instruction entity : entities) {
            InstructionDTO expectedDto = new InstructionDTO(
                    entity.getId(),
                    entity.getPayerFirstName(),
                    "SecondName",
                    "Patronymic",
                    "1234567890",
                    "1111222233334444",
                    "UA293123456789012345678901234",
                    "320000",
                    entity.getRecipientEdrpou(),
                    "Recipient Name",
                    entity.getAmount(),
                    1,
                    ChronoUnit.DAYS,
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(1),
                    InstructionStatus.ACTIVE
            );

            when(instructionMapper.toDTO(entity)).thenReturn(expectedDto);
        }

        mockMvc.perform(get("/instructions/search/edrpou/{edrpou}", testEdrpou)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(count)))
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

    @Test
    void shouldReturnActiveInstructionListAnd200() throws Exception {

        Long testId = 42L;

        Instruction entity = createInstructionEntity();
        InstructionDTO dto = createInstructionDTO(testId);

        when(instructionService.getAllActiveInstructions())
                .thenReturn(List.of(entity, entity));

        when(instructionMapper.toDTO(any(Instruction.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/instructions/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].payerIin", is("1111111118")))
                .andExpect(jsonPath("$[1].amount", is(500.50)));
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveInstructionsExist() throws Exception {

        when(instructionService.getAllActiveInstructions())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/instructions/all")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Instruction createInstructionEntity() {
        Instruction instruction = new Instruction();
        instruction.setPayerFirstName("NewName");
        instruction.setPayerSecondName("NewSecondName");
        instruction.setPayerPatronymic("Tarasovich");
        instruction.setAmount(new BigDecimal("500.50"));
        instruction.setPayerIin("1111111118");
        instruction.setPayerCardNumber("1234567812345678");
        instruction.setRecipientSettlementAccount("UA123456789012345678901234567");
        instruction.setRecipientBankCode("000000");
        instruction.setRecipientEdrpou("12345678");
        instruction.setRecipientName("Taras Ivanko");
        instruction.setPeriodUnit(ChronoUnit.DAYS);
        instruction.setPeriodValue(1);
        instruction.setNextExecutionAt(OffsetDateTime.now().plusDays(1));
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);
        return instruction;
    }

    private InstructionDTO createInstructionDTO(Long id) {
        return new InstructionDTO(
                id,
                "NewName",
                "NewSecondName",
                "Tarasovich",
                "1111111118",
                "1234567812345678",
                "UA123456789012345678901234567",
                "000000",
                "12345678",
                "Taras Ivanko",
                new BigDecimal("500.50"),
                1,
                ChronoUnit.DAYS,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1),
                InstructionStatus.ACTIVE
        );
    }

    private InstructionCreateDTO createInstructionCreateDTO() {
        return new InstructionCreateDTO(
                "NewName",
                "NewSecondName",
                "Tarasovich",
                "1111111118",
                "1234567812345678",
                "123456781234567812345678123",
                "000000",
                "12345678",
                "Taras Ivanko",
                new BigDecimal("500.50"),
                1,
                ChronoUnit.DAYS,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1),
                InstructionStatus.ACTIVE
        );
    }

    private List<Instruction> createInstructionListWithTestIin() {
        List<Instruction> instructions = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
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

package com.test.payment_pbls.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.InstructionCreateDTO;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(InstructionClient.class)
@TestPropertySource(properties = "application.server.pds=http://localhost:8180")
class InstructionClientTest {

    @Autowired
    private InstructionClient instructionClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    // Має співпадати з тим, що в @TestPropertySource
    private final String serverUrl = "http://localhost:8180";

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestClient restClient(RestClient.Builder builder) {
            return builder.build();
        }
    }

    @BeforeEach
    void setUp() {
        server.reset();
    }

    @Test
    void createInstruction_ShouldReturnCreatedInstruction_WhenServerReturnsSuccess() throws JsonProcessingException {

        InstructionCreateDTO createDto = createDto();
        Instruction expectedResponse = new Instruction();
        expectedResponse.setId(100L);
        expectedResponse.setPayerIin(createDto.payerIin());

        server.expect(requestTo(serverUrl + "/instructions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(createDto)))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        Instruction result = instructionClient.createInstruction(createDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        server.verify();
    }

    @Test
    void createInstruction_ShouldThrowCreationFailureException_WhenServerReturns4xx() {

        InstructionCreateDTO createDto = createDto();

        server.expect(requestTo(serverUrl + "/instructions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());


        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> instructionClient.createInstruction(createDto));

        assertTrue(exception.getMessage().contains("Failed to save instruction"));
        server.verify();
    }

    @Test
    void getInstructionsForIin_ShouldReturnList_WhenServerReturnsSuccess() throws JsonProcessingException {

        String testIin = "1234567890";
        List<Instruction> expectedList = List.of(new Instruction());

        server.expect(requestTo(serverUrl + "/instructions/search/iin/" + testIin))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedList), MediaType.APPLICATION_JSON));

        List<Instruction> result = instructionClient.getInstructionsForIin(testIin);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        server.verify();
    }

    @Test
    void getInstructionsForIin_ShouldThrowException_WhenServerReturns5xx() {

        String testIin = "1234567890";

        server.expect(requestTo(serverUrl + "/instructions/search/iin/" + testIin))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(CreationFailureException.class,
                () -> instructionClient.getInstructionsForIin(testIin));

        server.verify();
    }

    @Test
    void getInstructionsForEdrpou_ShouldReturnList_WhenServerReturnsSuccess() throws JsonProcessingException {

        String testEdrpou = "12345678";
        List<Instruction> expectedList = List.of(new Instruction(), new Instruction());

        server.expect(requestTo(serverUrl + "/instructions/search/edrpou/" + testEdrpou))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedList), MediaType.APPLICATION_JSON));

        List<Instruction> result = instructionClient.getInstructionsForEdrpou(testEdrpou);

        assertEquals(2, result.size());
        server.verify();
    }

    @Test
    void getScheduledInstructions_ShouldReturnList_WhenServerReturnsSuccess() throws JsonProcessingException {

        int page = 0;
        int size = 20;
        List<Instruction> expectedList = List.of(new Instruction());

        server.expect(requestTo(serverUrl + "/instructions/scheduled?page=" + page + "&size=" + size))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedList), MediaType.APPLICATION_JSON));

        List<Instruction> result = instructionClient.getScheduledInstructions(page, size);

        assertEquals(1, result.size());
        server.verify();
    }

    @Test
    void getScheduledInstructions_ShouldHandleUnexpectedError() {

        server.expect(requestTo(serverUrl + "/instructions/scheduled?page=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("INVALID JSON", MediaType.APPLICATION_JSON));

        CreationFailureException ex = assertThrows(CreationFailureException.class,
                () -> instructionClient.getScheduledInstructions(0, 10));

        assertEquals("Failed to search instruction in PDS: Service communication error.", ex.getMessage());
        server.verify();
    }

    private InstructionCreateDTO createDto() {
        return new InstructionCreateDTO(
                "John", "Doe", "Jr", "1234567890", "1234567812345678",
                "UA12345", "123456", "12345678", "Company",
                BigDecimal.TEN, 1, java.time.temporal.ChronoUnit.MONTHS,
                null, null, com.test.payment_pbls.utils.enums.InstructionStatus.ACTIVE
        );
    }
}
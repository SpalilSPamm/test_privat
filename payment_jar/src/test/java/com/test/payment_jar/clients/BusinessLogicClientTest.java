package com.test.payment_jar.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(components = BusinessLogicClient.class)
class BusinessLogicClientTest {

    @Autowired
    private BusinessLogicClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private final String serverUrl = "http://localhost:8181";

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
    void getScheduledInstructions_ShouldReturnList_WhenServerReturns200() throws JsonProcessingException {

        int page = 0;
        int size = 10;
        List<Instruction> expectedInstructions = List.of(
                createInstruction(1L, "100.00"),
                createInstruction(2L, "200.00")
        );
        String responseJson = objectMapper.writeValueAsString(expectedInstructions);

        server.expect(requestTo(serverUrl + "/instructions/scheduled?page=" + page + "&size=" + size))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<Instruction> result = client.getScheduledInstructions(page, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().getId());

        server.verify();
    }

    @Test
    void getScheduledInstructions_ShouldReturnEmptyList_WhenServerReturnsError() {

        int page = 0;
        int size = 10;

        server.expect(requestTo(serverUrl + "/instructions/scheduled?page=" + page + "&size=" + size))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        List<Instruction> result = client.getScheduledInstructions(page, size);


        assertNotNull(result);
        assertTrue(result.isEmpty());

        server.verify();
    }

    @Test
    void createTransactionsBatch_ShouldSucceed_WhenServerReturns200() throws JsonProcessingException {

        List<Instruction> instructions = List.of(createInstruction(1L, "100.00"));
        String requestJson = objectMapper.writeValueAsString(instructions);

        server.expect(requestTo(serverUrl + "/transactions/batch"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(requestJson))
                .andRespond(withSuccess()); // 200 OK (без тіла)

        assertDoesNotThrow(() -> client.createTransactionsBatch(instructions));

        server.verify();
    }

    @Test
    void createTransactionsBatch_ShouldThrowException_WhenServerReturnsError() throws JsonProcessingException {

        List<Instruction> instructions = List.of(createInstruction(1L, "100.00"));

        System.out.println(serverUrl);

        server.expect(requestTo(serverUrl + "/transactions/batch"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> client.createTransactionsBatch(instructions));

        assertEquals("Batch creation failed", exception.getMessage());

        server.verify();
    }
    
    private Instruction createInstruction(Long id, String amount) {
        Instruction instruction = new Instruction();
        instruction.setId(id);
        instruction.setAmount(new BigDecimal(amount));
        return instruction;
    }
}
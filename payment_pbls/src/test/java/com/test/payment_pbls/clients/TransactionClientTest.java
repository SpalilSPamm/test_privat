package com.test.payment_pbls.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.Transaction;
import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.utils.enums.TransactionStatus;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest(TransactionClient.class)
@TestPropertySource(properties = "application.server.pds=http://localhost:8180")
class TransactionClientTest {

    @Autowired
    private TransactionClient transactionClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createTransaction_ShouldReturnTransactionDTO_WhenServerReturnsSuccess() throws JsonProcessingException {

        Transaction transaction = createDummyTransaction();
        TransactionDTO expectedResponse = createDummyTransactionDTO();

        server.expect(requestTo(serverUrl + "/transactions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(transaction)))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        TransactionDTO result = transactionClient.createTransaction(transaction);

        assertNotNull(result);
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.amount(), result.amount());

        server.verify();
    }

    @Test
    void createTransaction_ShouldThrowCreationFailureException_WhenServerReturnsError() {

        Transaction transaction = createDummyTransaction();

        server.expect(requestTo(serverUrl + "/transactions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.createTransaction(transaction));

        assertEquals("Failed to save instruction in PDS: Service communication error.", exception.getMessage());
        server.verify();
    }

    @Test
    void revertTransaction_ShouldSucceed_WhenServerReturnsSuccess() {

        Long transactionId = 123L;

        server.expect(requestTo(serverUrl + "/transactions/" + transactionId))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> transactionClient.revertTransaction(transactionId));

        server.verify();
    }

    @Test
    void revertTransaction_ShouldThrowException_WhenServerReturnsError() {

        Long transactionId = 123L;

        server.expect(requestTo(serverUrl + "/transactions/" + transactionId))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withServerError());

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.revertTransaction(transactionId));

        assertTrue(exception.getMessage().contains("Failed to save instruction")); // Сообщение из вашего кода
        server.verify();
    }

    @Test
    void getTransactionsByInstructionId_ShouldReturnList_WhenServerReturnsSuccess() throws JsonProcessingException {

        Long instructionId = 10L;
        List<TransactionDTO> expectedList = List.of(createDummyTransactionDTO());

        server.expect(requestTo(serverUrl + "/transactions/instruction/" + instructionId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedList), MediaType.APPLICATION_JSON));

        List<TransactionDTO> result = transactionClient.getTransactionsByInstructionId(instructionId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedList.getFirst().id(), result.getFirst().id());

        server.verify();
    }

    @Test
    void getTransactionsByInstructionId_ShouldThrowException_WhenServerReturnsError() {

        Long instructionId = 10L;

        server.expect(requestTo(serverUrl + "/transactions/instruction/" + instructionId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.getTransactionsByInstructionId(instructionId));

        assertTrue(exception.getMessage().contains("Failed to search instruction"));
        server.verify();
    }

    private Transaction createDummyTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        Instruction instruction = new Instruction();
        instruction.setId(1L);
        transaction.setInstruction(instruction);
        return transaction;
    }

    private TransactionDTO createDummyTransactionDTO() {
        return new TransactionDTO(
                55L,
                1L,
                UUID.randomUUID().toString(),
                new BigDecimal("100.00"),
                OffsetDateTime.now(),
                TransactionStatus.ACTIVE.getStatusCode()
        );
    }
}
package com.test.payment_pbls.clients;

import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.models.Transaction;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionClient transactionClient;

    private static final String MOCK_PDS_URL = "http://localhost";
    private static final Long TRANSACTION_ID = 50L;
    private static final Long INSTRUCTION_ID = 10L;

    @BeforeEach
    void setUp() {
        transactionClient.serverUrl = MOCK_PDS_URL;
    }

    @Test
    void createTransaction_shouldReturnTransactionOnSuccess() {

        Transaction inputTransaction = createMockTransaction();

        when(restTemplate.postForEntity(
                eq(MOCK_PDS_URL + "/transactions"),
                eq(inputTransaction),
                eq(Transaction.class))
        ).thenReturn(new ResponseEntity<>(inputTransaction, HttpStatus.CREATED));

        Transaction result = transactionClient.createTransaction(inputTransaction);

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getId());
    }

    @Test
    void createTransaction_shouldThrowFailureExceptionOnRestClientError() {

        Transaction inputTransaction = createMockTransaction();

        when(restTemplate.postForEntity(
                eq(MOCK_PDS_URL + "/transactions"),
                eq(inputTransaction),
                eq(Transaction.class))
        ).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.createTransaction(inputTransaction));

        assert(exception.getMessage().contains("Service communication error."));
    }

    @Test
    void revertTransaction_shouldCallDeleteOnSuccess() {

        String expectedUrl = MOCK_PDS_URL + "/transactions/" + TRANSACTION_ID;

        assertDoesNotThrow(() -> transactionClient.revertTransaction(TRANSACTION_ID));

        verify(restTemplate).delete(eq(expectedUrl));
    }

    @Test
    void revertTransaction_shouldThrowFailureExceptionOnRestClientError() {

        Long transactionId = 50L;
        String expectedUrl = MOCK_PDS_URL + "/transactions/" + transactionId;

        doThrow(new RestClientException("PDS is unreachable."))
                .when(restTemplate).delete(eq(expectedUrl));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.revertTransaction(transactionId));

        assert(exception.getMessage().contains("Service communication error."));

        verify(restTemplate).delete(eq(expectedUrl));
    }

    @Test
    void getTransactionsByInstructionId_shouldReturnListOnSuccess() {

        List<Transaction> expectedList = List.of(createMockTransaction());
        ResponseEntity<List<Transaction>> mockResponse = new ResponseEntity<>(expectedList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(MOCK_PDS_URL + "/transactions/instruction/" + INSTRUCTION_ID),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<Transaction> result = transactionClient.getTransactionsByInstructionId(INSTRUCTION_ID);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByInstructionId_shouldThrowFailureExceptionOnUnexpectedError() {

        when(restTemplate.exchange(
                eq(MOCK_PDS_URL + "/transactions/instruction/" + INSTRUCTION_ID),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Parsing error in response."));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> transactionClient.getTransactionsByInstructionId(INSTRUCTION_ID));

        assert(exception.getMessage().contains("An unexpected error occurred during instruction search."));
    }

    private Transaction createMockTransaction() {

        Instruction inputInstruction = new Instruction();
        inputInstruction.setId(100L);

        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setInstruction(inputInstruction);
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setIdempotencyId(UUID.randomUUID().toString());
        return transaction;
    }
}

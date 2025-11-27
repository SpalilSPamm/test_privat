package com.test.payment_jar.clients;

import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.enums.InstructionStatus;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessLogicClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BusinessLogicClient businessLogicClient;

    private static final String SERVER_URL = "http://localhost:8181";
    private Instruction mockInstruction;

    @BeforeEach
    void setUp() {
        businessLogicClient = new BusinessLogicClient(restTemplate, "http://localhost:8181");
        mockInstruction = createMockInstruction();
    }

    @Test
    void getAllActiveInstructions_shouldReturnListOnSuccess() {

        List<Instruction> expectedList = List.of(mockInstruction);
        ResponseEntity<List<Instruction>> mockResponse = new ResponseEntity<>(expectedList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(SERVER_URL + "/instructions/all"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<Instruction> result = businessLogicClient.getAllActiveInstructions();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(mockInstruction.getId(), result.getFirst().getId());

        verify(restTemplate, times(1)).exchange(
                eq(SERVER_URL + "/instructions/all"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getAllActiveInstructions_shouldReturnEmptyListOnRestClientException() {

        when(restTemplate.exchange(
                eq(SERVER_URL + "/instructions/all"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection refused."));

        List<Instruction> result = businessLogicClient.getAllActiveInstructions();

        assertTrue(result.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllActiveInstructions_shouldReturnEmptyListOnGeneralException() {

        when(restTemplate.exchange(
                eq(SERVER_URL + "/instructions/all"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Unexpected error."));

        List<Instruction> result = businessLogicClient.getAllActiveInstructions();

        assertTrue(result.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void createTransaction_shouldCompleteSuccessfully() {

        when(restTemplate.postForEntity(
                eq(SERVER_URL + "/transactions"),
                eq(mockInstruction),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        assertDoesNotThrow(() -> businessLogicClient.createTransaction(mockInstruction));

        verify(restTemplate, times(1)).postForEntity(
                eq(SERVER_URL + "/transactions"),
                eq(mockInstruction),
                eq(Void.class)
        );
    }

    @Test
    void createTransaction_shouldThrowCreationFailureExceptionOnRestClientError() {

        when(restTemplate.postForEntity(
                eq(SERVER_URL + "/transactions"),
                eq(mockInstruction),
                eq(Void.class)
        )).thenThrow(new RestClientException("Service communication error."));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> businessLogicClient.createTransaction(mockInstruction));

        assertTrue(exception.getMessage().contains("Service communication error."));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Void.class));
    }

    @Test
    void createTransaction_shouldThrowCreationFailureExceptionOnGeneralException() {

        when(restTemplate.postForEntity(
                eq(SERVER_URL + "/transactions"),
                eq(mockInstruction),
                eq(Void.class)
        )).thenThrow(new RuntimeException("JSON serialization error."));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> businessLogicClient.createTransaction(mockInstruction));

        assertTrue(exception.getMessage().contains("An unexpected error occurred during instruction creation."));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Void.class));
    }

    private Instruction createMockInstruction() {
        Instruction instruction = new Instruction();
        instruction.setId(1L);
        instruction.setAmount(new BigDecimal("100.00"));
        instruction.setInstructionStatus(InstructionStatus.ACTIVE);
        instruction.setNextExecutionAt(OffsetDateTime.now().plus(1, ChronoUnit.HOURS));
        return instruction;
    }

}

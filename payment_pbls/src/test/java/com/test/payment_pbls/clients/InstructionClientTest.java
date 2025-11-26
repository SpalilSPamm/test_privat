package com.test.payment_pbls.clients;

import com.test.payment_pbls.models.Instruction;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InstructionClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InstructionClient instructionClient;

    private static final String MOCK_SERVER_URL = "http://locakhost";
    private static final String TEST_IIN = "1234567899";
    private static final String TEST_EDRPOU = "40087654";


    @BeforeEach
    void setUp() {
        instructionClient.serverUrl = MOCK_SERVER_URL;
    }

    @Test
    void createInstruction_shouldReturnInstructionOnSuccess() {

        Instruction inputInstruction = new Instruction();
        inputInstruction.setPayerIin(TEST_IIN);

        Instruction expectedInstruction = new Instruction();
        expectedInstruction.setId(10L);
        expectedInstruction.setPayerIin(TEST_IIN);

        when(restTemplate.postForEntity(
                eq(MOCK_SERVER_URL + "/instructions"),
                eq(inputInstruction),
                eq(Instruction.class))
        ).thenReturn(new ResponseEntity<>(expectedInstruction, HttpStatus.CREATED));

        Instruction result = instructionClient.createInstruction(inputInstruction);

        assertNotNull(result);
        assertEquals(expectedInstruction.getId(), result.getId());
    }

    @Test
    void createInstruction_shouldThrowFailureExceptionOnRestClientError() {

        Instruction inputInstruction = new Instruction();

        when(restTemplate.postForEntity(
                eq(MOCK_SERVER_URL + "/instructions"),
                eq(inputInstruction),
                eq(Instruction.class))
        ).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> instructionClient.createInstruction(inputInstruction));

        assert(exception.getMessage().contains("Service communication error."));
    }

    @Test
    void createInstruction_shouldThrowFailureExceptionOnUnexpectedError() {

        Instruction inputInstruction = new Instruction();

        when(restTemplate.postForEntity(
                eq(MOCK_SERVER_URL + "/instructions"),
                eq(inputInstruction),
                eq(Instruction.class))
        ).thenThrow(new RuntimeException("Parsing failure"));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> instructionClient.createInstruction(inputInstruction));

        assert(exception.getMessage().contains("An unexpected error occurred during instruction creation."));
    }

    @Test
    void getInstructionsForIin_shouldReturnListOnSuccess() {

        List<Instruction> expectedList = List.of(new Instruction());
        ResponseEntity<List<Instruction>> mockResponse = new ResponseEntity<>(expectedList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(MOCK_SERVER_URL + "/instructions/search/iin/" + TEST_IIN),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<Instruction> result = instructionClient.getInstructionsForIin(TEST_IIN);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getInstructionsForIin_shouldThrowFailureExceptionOnRestClientError() {

        when(restTemplate.exchange(
                eq(MOCK_SERVER_URL + "/instructions/search/iin/" + TEST_IIN),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection timed out"));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> instructionClient.getInstructionsForIin(TEST_IIN));

        assert(exception.getMessage().contains("Service communication error."));
    }

    @Test
    void getInstructionsForEdrpou_shouldReturnListOnSuccess() {

        List<Instruction> expectedList = List.of(new Instruction());
        ResponseEntity<List<Instruction>> mockResponse = new ResponseEntity<>(expectedList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(MOCK_SERVER_URL + "/instructions/search/edrpou/" + TEST_EDRPOU),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<Instruction> result = instructionClient.getInstructionsForEdrpou(TEST_EDRPOU);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getInstructionsForEdrpou_shouldThrowFailureExceptionOnUnexpectedError() {

        when(restTemplate.exchange(
                eq(MOCK_SERVER_URL + "/instructions/search/edrpou/" + TEST_EDRPOU),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Parsing failure"));

        CreationFailureException exception = assertThrows(CreationFailureException.class,
                () -> instructionClient.getInstructionsForEdrpou(TEST_EDRPOU));

        assert(exception.getMessage().contains("An unexpected error occurred during instruction search."));
    }
}

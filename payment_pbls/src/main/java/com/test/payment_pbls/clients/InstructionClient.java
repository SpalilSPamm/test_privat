package com.test.payment_pbls.clients;

import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.InstructionCreateDTO;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class InstructionClient {

    private final RestClient restClient;
    private final String serverUrl;

    @Autowired
    public InstructionClient(RestClient restClient, @Value("${application.server.pds}") String url) {
        this.restClient = restClient;
        this.serverUrl = url;
    }

    public Instruction createInstruction(InstructionCreateDTO instructionCreateDTO) {
        try {
            return restClient.post()
                    .uri(serverUrl + "/instructions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(instructionCreateDTO)
                    .retrieve()
                    .body(Instruction.class);

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }

    public List<Instruction> getInstructionsForIin(String iin) {
        try {
            return restClient.get()
                    // Використовуємо {placeholders} замість .formatted() - це безпечніше і правильніше
                    .uri(serverUrl + "/instructions/search/iin/{iin}", iin)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }

    public List<Instruction> getInstructionsForEdrpou(String edrpou) {
        try {
            return restClient.get()
                    .uri(serverUrl + "/instructions/search/edrpou/{edrpou}", edrpou)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }

    public List<Instruction> getScheduledInstructions(int page, int size) {
        try {
            return restClient.get()
                    .uri(serverUrl + "/instructions/scheduled?page={page}&size={size}", page, size)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }
}
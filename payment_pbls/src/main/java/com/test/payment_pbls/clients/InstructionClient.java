package com.test.payment_pbls.clients;

import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class InstructionClient {

    RestTemplate restTemplate;
    String serverUrl;

    @Autowired
    public InstructionClient(RestTemplate restTemplate, @Value("${application.server.pds}") String url) {
        this.restTemplate = restTemplate;
        this.serverUrl = url;
    }

    public Instruction createInstruction(Instruction instruction) {

        try {

            return restTemplate.postForEntity(serverUrl + "/instructions", instruction, Instruction.class).getBody();

//        } catch (DataIntegrityViolationException e) {
//            // Це може статися, якщо PBLS не перехопив порушення UNIQUE INDEX
//            // або NOT NULL обмеження.
//            throw new CreationFailureException("PDS rejected instruction due to data integrity constraints.", e);

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }

    public List<Instruction> getInstructionsForIin(String iin) {

        try {

            ResponseEntity<List<Instruction>> response = restTemplate.exchange(
                    serverUrl + "/instructions/search/iin/%s".formatted(iin),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }

    public List<Instruction> getInstructionsForEdrpou(String edrpou) {

        try {
            ResponseEntity<List<Instruction>> response = restTemplate.exchange(
                    serverUrl + "/instructions/search/edrpou/%s".formatted(edrpou),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }

    public List<Instruction> getAllActiveInstructions() {

        try {
            ResponseEntity<List<Instruction>> response = restTemplate.exchange(
                    serverUrl + "/instructions/all",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }
}

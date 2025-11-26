package com.test.payment_jar.clients;

import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class BusinessLogicClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;


    @Autowired
    public BusinessLogicClient(RestTemplate restTemplate, @Value("${application.server.pbls}")  String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
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
            log.error("Failed to search instruction in PBLS: Service communication error.");
            return List.of();
        } catch (Exception e) {
            log.error("An unexpected error occurred during instruction search.");
            return List.of();
        }
    }

    public void createTransaction(Instruction instruction) {

        try {

            restTemplate.postForEntity(serverUrl + "/transactions", instruction, Void.class);

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }
}

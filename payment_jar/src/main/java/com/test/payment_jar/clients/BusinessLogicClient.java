package com.test.payment_jar.clients;

import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class BusinessLogicClient {

    private final RestClient restClient;
    private final String serverUrl;


    @Autowired
    public BusinessLogicClient(RestClient restClient, @Value("${application.server.pbls}") String serverUrl) {
        this.restClient = restClient;
        this.serverUrl = serverUrl;
    }

    public List<Instruction> getScheduledInstructions(int page, int size) {
        try {
            String uri = UriComponentsBuilder.fromUriString(serverUrl)
                    .path("/instructions/scheduled")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .toUriString();

            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to fetch instructions from PBLS", e);
            return List.of();
        }
    }

    public void createTransactionsBatch(List<Instruction> instructions) {
        try {
            String uri = UriComponentsBuilder.fromUriString(serverUrl)
                    .path("/transactions/batch")
                    .toUriString();
            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(instructions)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send batch transactions", e);
            throw new CreationFailureException("Batch creation failed");
        }
    }
}
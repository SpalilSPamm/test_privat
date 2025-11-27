package com.test.payment_pbls.clients;

import com.test.payment_pbls.dtos.Transaction;
import com.test.payment_pbls.dtos.TransactionDTO;
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
public class TransactionClient {

    private final RestClient restClient;
    private final String serverUrl;

    @Autowired
    public TransactionClient(RestClient restClient, @Value("${application.server.pds}") String url) {
        this.restClient = restClient;
        this.serverUrl = url;
    }

    public TransactionDTO createTransaction(Transaction transaction) {
        try {
            return restClient.post()
                    .uri(serverUrl + "/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(transaction)
                    .retrieve()
                    .body(TransactionDTO.class);

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }

    public void revertTransaction(Long transactionId) {
        try {
            restClient.delete()
                    .uri(serverUrl + "/transactions/{id}", transactionId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }

    public List<TransactionDTO> getTransactionsByInstructionId(Long instructionId) {
        try {
            return restClient.get()
                    .uri(serverUrl + "/transactions/instruction/{instructionId}", instructionId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to search instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction search.");
        }
    }
}
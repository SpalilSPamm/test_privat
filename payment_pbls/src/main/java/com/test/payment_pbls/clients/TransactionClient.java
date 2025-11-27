package com.test.payment_pbls.clients;

import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.dtos.Transaction;
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
public class TransactionClient {

    RestTemplate restTemplate;
    String serverUrl;

    @Autowired
    public TransactionClient(RestTemplate restTemplate, @Value("${application.server.pds}") String url) {
        this.restTemplate = restTemplate;
        this.serverUrl = url;
    }

    public TransactionDTO createTransaction(Transaction transaction) {

        try {

            return restTemplate.postForEntity(serverUrl + "/transactions", transaction, TransactionDTO.class).getBody();

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

    public void revertTransaction(Long transactionId) {

        try {

            restTemplate.delete(serverUrl + "/transactions/%s".formatted(transactionId));

        } catch (RestClientException e) {
            throw new CreationFailureException("Failed to save instruction in PDS: Service communication error.");
        } catch (Exception e) {
            throw new CreationFailureException("An unexpected error occurred during instruction creation.");
        }
    }

    public List<TransactionDTO> getTransactionsByInstructionId(Long instructionId) {

        try {

            ResponseEntity<List<TransactionDTO>> response = restTemplate.exchange(
                    serverUrl + "/transactions/instruction/%s".formatted(instructionId),
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

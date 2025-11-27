package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.TransactionClient;
import com.test.payment_pbls.dtos.BatchResultDTO;
import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.dtos.Transaction;
import com.test.payment_pbls.services.TransactionService;
import com.test.payment_pbls.utils.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final Clock clock;
    private final TransactionClient transactionClient;

    @Autowired
    public TransactionServiceImpl(Clock clock, TransactionClient transactionClient) {
        this.clock = clock;
        this.transactionClient = transactionClient;
    }


    @Override
    public TransactionDTO createTransaction(Instruction instruction) {

        log.info("PBLS: Update last and next execution date for instruction ID: {}", instruction.getId());

        instruction.setLastExecutionAt(OffsetDateTime.now(clock));
        instruction.setNextExecutionAt(OffsetDateTime.now(clock).plus(instruction.getPeriodValue(), instruction.getPeriodUnit()));

        log.info("PBLS: Initiating transaction creation for instruction ID: {}", instruction.getId());

        Transaction transaction = new Transaction();

        transaction.setInstruction(instruction);

        transaction.setIdempotencyId(UUID.randomUUID().toString());

        transaction.setTransactionStatus(TransactionStatus.ACTIVE.getStatusCode());
        transaction.setTransactionTime(OffsetDateTime.now(clock));
        transaction.setAmount(instruction.getAmount());

        TransactionDTO savedTransaction = transactionClient.createTransaction(transaction);

        log.info("PBLS: Transaction successfully saved in PDS (ID: {}, Key: {}).",
                savedTransaction.id(), savedTransaction.idempotencyId());

        return savedTransaction;
    }

    @Override
    public void revertTransaction(Long transactionId) {

        log.info("PBLS: Initiating STORNU (reversal) for transaction ID: {}", transactionId);

        transactionClient.revertTransaction(transactionId);

        log.info("PBLS: Reversal request successfully sent to PDS for ID: {}", transactionId);
    }

    @Override
    public List<TransactionDTO> getInstructionHistory(Long instructionId) {
        log.debug("PBLS: Retrieving transaction history for instruction ID: {}", instructionId);

        List<TransactionDTO> history = transactionClient.getTransactionsByInstructionId(instructionId);

        log.debug("PBLS: Retrieved {} transactions.", history.size());

        return history;
    }

    @Override
    public BatchResultDTO processBatch(List<Instruction> instructions) {
        int success = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Instruction instruction : instructions) {
            try {
                createTransaction(instruction);
                success++;
            } catch (Exception e) {
                log.error("Failed to process instruction {}", instruction.getId(), e);
                failedIds.add(instruction.getId());
            }
        }

        return new BatchResultDTO(success, failedIds.size(), failedIds);
    }
}

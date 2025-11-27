package com.test.payment_pbls.services;

import com.test.payment_pbls.dtos.BatchResultDTO;
import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.dtos.Instruction;

import java.util.List;

public interface TransactionService {

    TransactionDTO createTransaction(Instruction instruction);
    void revertTransaction(Long transactionId);
    List<TransactionDTO> getInstructionHistory(Long instructionId);
    BatchResultDTO processBatch(List<Instruction> instructions);
}

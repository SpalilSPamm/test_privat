package com.test.payment_pbls.services;

import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.models.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Instruction instruction);
    void revertTransaction(Long transactionId);
    List<Transaction> getInstructionHistory(Long instructionId);
}

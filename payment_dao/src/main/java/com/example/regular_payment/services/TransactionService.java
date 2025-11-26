package com.example.regular_payment.services;

import com.example.regular_payment.models.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(Long id, Transaction transaction);

    void deleteTransaction(Long id);

    Transaction getTransaction(Long id);

    List<Transaction> getTransactionsByInstruction(Long instructionId);
}

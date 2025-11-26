package com.example.regular_payment.services.impl;

import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.repositories.TransactionRepository;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.services.TransactionService;
import com.example.regular_payment.utils.enums.TransactionStatus;
import com.example.regular_payment.utils.exceptions.TransactionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final InstructionService instructionService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  InstructionService instructionService) {
        this.transactionRepository = transactionRepository;
        this.instructionService = instructionService;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {

        instructionService.updateLastAndNextRExecutionTime(transaction.getInstruction());

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Long id, Transaction transaction) {

        Transaction existTransaction = getTransaction(id);

        existTransaction.setTransactionStatus(transaction.getTransactionStatus());
        existTransaction.setAmount(transaction.getAmount());

        return transactionRepository.save(existTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {

        Transaction transaction = getTransaction(id);

        transaction.setTransactionStatus(TransactionStatus.REVERSED.getStatusCode());

        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with ID " + id + " not found in PDS."));
    }

    @Override
    @Transactional
    public List<Transaction> getTransactionsByInstruction(Long instructionId) {
        return transactionRepository.getTransactionsByInstruction_Id(instructionId);
    }
}

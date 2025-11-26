package com.example.regular_payment.controllers;

import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {

        Transaction savedTransaction = transactionService.createTransaction(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction updatedTransaction) {

        Transaction savedTransaction = transactionService.updateTransaction(id, updatedTransaction);

        return ResponseEntity.status(HttpStatus.OK).body(savedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTransaction(id));
    }

    @GetMapping("/instruction/{instructionId}")
    public  ResponseEntity<List<Transaction>> getTransactionsByInstruction(@PathVariable Long instructionId) {

        List<Transaction> result = transactionService.getTransactionsByInstruction(instructionId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

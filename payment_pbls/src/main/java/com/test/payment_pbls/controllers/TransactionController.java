package com.test.payment_pbls.controllers;

import com.test.payment_pbls.models.Instruction;
import com.test.payment_pbls.models.Transaction;
import com.test.payment_pbls.services.TransactionService;
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
    public ResponseEntity<Transaction> createTransactionByInstruction(@RequestBody Instruction instruction) {

        Transaction transaction = transactionService.createTransaction(instruction);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PatchMapping("/revert/{transactionId}")
    public ResponseEntity<Object> revertTransaction(@PathVariable Long transactionId) {

        transactionService.revertTransaction(transactionId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{instructionId}/history")
    public ResponseEntity<List<Transaction>>  getInstructionHistory(@PathVariable Long instructionId) {

        List<Transaction> result = transactionService.getInstructionHistory(instructionId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

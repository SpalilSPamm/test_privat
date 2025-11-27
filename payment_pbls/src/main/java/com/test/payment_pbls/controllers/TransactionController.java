package com.test.payment_pbls.controllers;

import com.test.payment_pbls.dtos.TransactionDTO;
import com.test.payment_pbls.dtos.Instruction;
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
    public ResponseEntity<TransactionDTO> createTransactionByInstruction(@RequestBody Instruction instruction) {

        TransactionDTO transaction = transactionService.createTransaction(instruction);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PatchMapping("/revert/{transactionId}")
    public ResponseEntity<Object> revertTransaction(@PathVariable Long transactionId) {

        transactionService.revertTransaction(transactionId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{instructionId}/history")
    public ResponseEntity<List<TransactionDTO>>  getInstructionHistory(@PathVariable Long instructionId) {

        List<TransactionDTO> result = transactionService.getInstructionHistory(instructionId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

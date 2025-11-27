package com.example.regular_payment.controllers;

import com.example.regular_payment.dtos.TransactionCreateDTO;
import com.example.regular_payment.dtos.TransactionDTO;
import com.example.regular_payment.models.Transaction;
import com.example.regular_payment.services.TransactionService;
import com.example.regular_payment.utils.mappers.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionMapper transactionMapper;
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionMapper transactionMapper,
                                 TransactionService transactionService) {
        this.transactionMapper = transactionMapper;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionCreateDTO transactionCreateDTO) {

        Transaction transaction = transactionMapper.toEntity(transactionCreateDTO);

        Transaction savedTransaction = transactionService.createTransaction(transaction);

        TransactionDTO transactionDTO = transactionMapper.toDTO(savedTransaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody TransactionDTO transactionDTO) {

        Transaction updatedTransaction = transactionMapper.toEntity(transactionDTO);

        Transaction savedTransaction = transactionService.updateTransaction(id, updatedTransaction);

        TransactionDTO savedTransactionDTO = transactionMapper.toDTO(savedTransaction);

        return ResponseEntity.status(HttpStatus.OK).body(savedTransactionDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {

        TransactionDTO transactionDTO = transactionMapper.toDTO(transactionService.getTransaction(id));

        return ResponseEntity.status(HttpStatus.OK).body(transactionDTO);
    }

    @GetMapping("/instruction/{instructionId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByInstruction(@PathVariable Long instructionId) {

        List<TransactionDTO> result = transactionService.getTransactionsByInstruction(instructionId)
                .stream().map(transactionMapper::toDTO).toList();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TransactionDTO>> createTransactionsBatch(@RequestBody List<TransactionCreateDTO> batchDtos) {

        List<Transaction> transactions = batchDtos.stream().map(transactionMapper::toEntity).toList();

        List<Transaction> savedTransactions = transactionService.createTransactionsBatch(transactions);

        List<TransactionDTO> responseDtos = savedTransactions.stream().map(transactionMapper::toDTO).toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtos);
    }
}

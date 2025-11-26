package com.example.regular_payment.repositories;

import com.example.regular_payment.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> getTransactionsByInstruction_Id(Long instructionId);
}

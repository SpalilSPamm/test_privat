package com.example.regular_payment.repositories;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.utils.enums.InstructionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructionRepository extends JpaRepository<Instruction, Long> {

    List<Instruction> getInstructionsByPayerIin(String payerIin);
    List<Instruction> getInstructionsByRecipientEdrpou(String recipientEdrpou);
    List<Instruction> getInstructionsByInstructionStatus(InstructionStatus instructionStatus);
}

package com.example.regular_payment.repositories;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.utils.enums.InstructionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface InstructionRepository extends JpaRepository<Instruction, Long> {

    List<Instruction> getInstructionsByPayerIin(String payerIin);
    List<Instruction> getInstructionsByRecipientEdrpou(String recipientEdrpou);
    Slice<Instruction> findByInstructionStatusAndNextExecutionAtBefore(
            InstructionStatus status,
            OffsetDateTime nextExecutionAt,
            Pageable pageable
    );
}

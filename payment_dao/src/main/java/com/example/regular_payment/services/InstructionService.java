package com.example.regular_payment.services;

import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.OffsetDateTime;
import java.util.List;

public interface InstructionService {

    Instruction saveInstruction(Instruction instruction);

    Instruction updateInstruction(Long id, InstructionDTO instructionDTO);

    void updateLastAndNextExecutionTime(Long id, OffsetDateTime lastExecutionAt, OffsetDateTime nextExecutionAt);

    void deleteInstruction(Long id);

    Instruction getInstruction(Long id);
    List<Instruction> getInstructionsByIin(String payerIin);
    List<Instruction> getInstructionsByEdrpou(String recipientEdrpou);

    Slice<Instruction> getScheduledInstructions(Pageable pageable);

}

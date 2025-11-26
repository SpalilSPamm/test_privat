package com.example.regular_payment.services;

import com.example.regular_payment.models.Instruction;

import java.util.List;

public interface InstructionService {

    Instruction saveInstruction(Instruction instruction);

    Instruction updateInstruction(Long id, Instruction instruction);

    void updateLastAndNextRExecutionTime(Instruction instruction);

    void deleteInstruction(Long id);

    Instruction getInstruction(Long id);
    List<Instruction> getInstructionsByIin(String payerIin);
    List<Instruction> getInstructionsByEdrpou(String recipientEdrpou);

    List<Instruction> getAllActiveInstructions();

}

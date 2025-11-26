package com.test.payment_pbls.services;

import com.test.payment_pbls.dtos.InstructionDTO;
import com.test.payment_pbls.models.Instruction;

import java.util.List;

public interface InstructionService {

    Instruction createInstruction(InstructionDTO instructionDTO);
    List<Instruction> getInstructionsByPayerIin(String payerIin);
    List<Instruction> getInstructionsByRecipientEdrpou(String recipientEdrpou);
    List<Instruction> getAllActiveInstruction();
}

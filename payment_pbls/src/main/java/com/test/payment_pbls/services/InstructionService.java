package com.test.payment_pbls.services;

import com.test.payment_pbls.dtos.InstructionValidDTO;
import com.test.payment_pbls.dtos.Instruction;

import java.util.List;

public interface InstructionService {

    Instruction createInstruction(InstructionValidDTO instructionValidDTO);
    List<Instruction> getInstructionsByPayerIin(String payerIin);
    List<Instruction> getInstructionsByRecipientEdrpou(String recipientEdrpou);
    List<Instruction> getScheduledInstructions(int page, int size);
}

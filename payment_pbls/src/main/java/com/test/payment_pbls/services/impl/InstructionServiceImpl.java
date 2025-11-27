package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.InstructionClient;
import com.test.payment_pbls.dtos.InstructionCreateDTO;
import com.test.payment_pbls.dtos.InstructionValidDTO;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.services.InstructionService;
import com.test.payment_pbls.services.ValidationService;
import com.test.payment_pbls.utils.enums.InstructionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InstructionServiceImpl implements InstructionService {


    private final Clock clock;
    private final InstructionClient instructionClient;
    private final ValidationService validationService;

    @Autowired
    public InstructionServiceImpl(Clock clock, InstructionClient instructionClient, ValidationService validationService) {
        this.clock = clock;
        this.instructionClient = instructionClient;
        this.validationService = validationService;
    }

    @Override
    public Instruction createInstruction(InstructionValidDTO instructionValidDTO) {

        validationService.validatePayerIinChecksum(instructionValidDTO.payerIin());
        validationService.validatePayerEdrpouChecksum(instructionValidDTO.recipientEdrpou());

        InstructionCreateDTO instructionCreateDTO = new InstructionCreateDTO(
                instructionValidDTO.payerFirstName(),
                instructionValidDTO.payerSecondName(),
                instructionValidDTO.payerPatronymic(),
                instructionValidDTO.payerIin(),
                instructionValidDTO.payerCardNumber(),
                instructionValidDTO.recipientSettlementAccount(),
                instructionValidDTO.recipientBankCode(),
                instructionValidDTO.recipientEdrpou(),
                instructionValidDTO.recipientName(),
                instructionValidDTO.amount(),
                instructionValidDTO.periodValue(),
                instructionValidDTO.periodUnit(),
                null,
                OffsetDateTime.now(clock).plus(instructionValidDTO.periodValue(), instructionValidDTO.periodUnit()),
                InstructionStatus.ACTIVE
        );

        return instructionClient.createInstruction(instructionCreateDTO);
    }

    @Override
    public List<Instruction> getInstructionsByPayerIin(String payerIin) {

        validationService.validatePayerIinChecksum(payerIin);

        return instructionClient.getInstructionsForIin(payerIin);
    }

    @Override
    public List<Instruction> getInstructionsByRecipientEdrpou(String recipientEdrpou) {

        validationService.validatePayerEdrpouChecksum(recipientEdrpou);

        return instructionClient.getInstructionsForEdrpou(recipientEdrpou);
    }

    @Override
    public List<Instruction> getAllActiveInstruction() {
        return instructionClient.getAllActiveInstructions();
    }
}

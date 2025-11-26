package com.test.payment_pbls.services.impl;

import com.test.payment_pbls.clients.InstructionClient;
import com.test.payment_pbls.dtos.InstructionDTO;
import com.test.payment_pbls.models.Instruction;
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


    private Clock clock;
    private InstructionClient instructionClient;
    private ValidationService validationService;

    @Autowired
    public InstructionServiceImpl(Clock clock, InstructionClient instructionClient, ValidationService validationService) {
        this.clock = clock;
        this.instructionClient = instructionClient;
        this.validationService = validationService;
    }

    @Override
    public Instruction createInstruction(InstructionDTO instructionDTO) {

        validationService.validatePayerIinChecksum(instructionDTO.payerIin());
        validationService.validatePayerEdrpouChecksum(instructionDTO.recipientEdrpou());

        Instruction instruction = new Instruction();

        instruction.setPayerFirstName(instructionDTO.payerFirstName());
        instruction.setPayerSecondName(instructionDTO.payerSecondName());
        instruction.setPayerPatronymic(instructionDTO.payerPatronymic());

        instruction.setPayerIin(instructionDTO.payerIin());
        instruction.setPayerCardNumber(instructionDTO.payerCardNumber());

        instruction.setRecipientSettlementAccount(instructionDTO.recipientSettlementAccount());
        instruction.setRecipientBankCode(instructionDTO.recipientBankCode());
        instruction.setRecipientEdrpou(instructionDTO.recipientEdrpou());
        instruction.setRecipientName(instructionDTO.recipientName());

        instruction.setAmount(instructionDTO.amount());
        instruction.setPeriodValue(instructionDTO.periodValue());
        instruction.setPeriodUnit(instructionDTO.periodUnit());

        instruction.setNextExecutionAt(
                OffsetDateTime.now(clock).plus(instructionDTO.periodValue(), instructionDTO.periodUnit())
        );

        instruction.setInstructionStatus(InstructionStatus.ACTIVE);

        return instructionClient.createInstruction(instruction);
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

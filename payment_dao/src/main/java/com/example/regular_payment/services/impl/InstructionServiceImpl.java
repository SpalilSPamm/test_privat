package com.example.regular_payment.services.impl;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.repositories.InstructionRepository;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InstructionServiceImpl implements InstructionService {

    private final Clock clock;
    private final InstructionRepository instructionRepository;

    @Autowired
    public InstructionServiceImpl(Clock clock, InstructionRepository instructionRepository) {
        this.clock = clock;
        this.instructionRepository = instructionRepository;
    }

    @Override
    @Transactional
    public Instruction saveInstruction(Instruction instruction) {
        return instructionRepository.save(instruction);
    }

    @Override
    @Transactional
    public Instruction updateInstruction(Long id, Instruction instruction) {
        Instruction existingInstruction = getInstruction(id);

        existingInstruction.setPayerFirstName(instruction.getPayerFirstName());
        existingInstruction.setPayerSecondName(instruction.getPayerSecondName());
        existingInstruction.setPayerPatronymic(instruction.getPayerPatronymic());
        existingInstruction.setPayerIin(instruction.getPayerIin());
        existingInstruction.setPayerCardNumber(instruction.getPayerCardNumber());

        existingInstruction.setRecipientSettlementAccount(instruction.getRecipientSettlementAccount());
        existingInstruction.setRecipientBankCode(instruction.getRecipientBankCode());
        existingInstruction.setRecipientEdrpou(instruction.getRecipientEdrpou());
        existingInstruction.setRecipientName(instruction.getRecipientName());

        existingInstruction.setPeriodValue(instruction.getPeriodValue());
        existingInstruction.setPeriodUnit(instruction.getPeriodUnit());
        existingInstruction.setAmount(instruction.getAmount());

        existingInstruction.setNextExecutionAt(instruction.getNextExecutionAt());
        existingInstruction.setLastExecutionAt(instruction.getLastExecutionAt());

        return instructionRepository.save(existingInstruction);
    }

    @Override
    @Transactional
    public void updateLastAndNextRExecutionTime(Instruction instruction) {

        Long instructionId = instruction != null ? instruction.getId() : null;

        if (instructionId == null || !instructionRepository.existsById(instruction.getId())) {
            throw new InstructionNotFoundException("Instruction with ID " + instructionId + " not found in PDS.");
        }

        instruction.setLastExecutionAt(OffsetDateTime.now(clock));
        instruction.setNextExecutionAt(OffsetDateTime.now(clock).plus(instruction.getPeriodValue(), instruction.getPeriodUnit()));

        instructionRepository.save(instruction);
    }

    @Override
    @Transactional
    public void deleteInstruction(Long id) {

        Instruction existingInstruction = getInstruction(id);

        existingInstruction.setInstructionStatus(InstructionStatus.CANCELED);

        instructionRepository.save(existingInstruction);
    }

    @Override
    @Transactional(readOnly = true)
    public Instruction getInstruction(Long id) {
        return instructionRepository.findById(id)
                .orElseThrow(() -> new InstructionNotFoundException("Instruction with ID " + id + " not found in PDS."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instruction> getInstructionsByIin(String payerIin) {
        return instructionRepository.getInstructionsByPayerIin(payerIin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instruction> getInstructionsByEdrpou(String recipientEdrpou) {
        return instructionRepository.getInstructionsByRecipientEdrpou(recipientEdrpou);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instruction> getAllActiveInstructions() {
        return instructionRepository.getInstructionsByInstructionStatus(InstructionStatus.ACTIVE);
    }
}

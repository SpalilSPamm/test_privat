package com.example.regular_payment.services.impl;

import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.repositories.InstructionRepository;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.utils.enums.InstructionStatus;
import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;

import com.example.regular_payment.utils.mappers.InstructionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InstructionServiceImpl implements InstructionService {

    private final Clock clock;
    private final InstructionMapper instructionMapper;
    private final InstructionRepository instructionRepository;

    @Autowired
    public InstructionServiceImpl(Clock clock,
                                  InstructionMapper instructionMapper,
                                  InstructionRepository instructionRepository) {
        this.clock = clock;
        this.instructionMapper = instructionMapper;
        this.instructionRepository = instructionRepository;
    }

    @Override
    @Transactional
    public Instruction saveInstruction(Instruction instruction) {
        return instructionRepository.save(instruction);
    }

    @Override
    @Transactional
    public Instruction updateInstruction(Long id, InstructionDTO instructionDTO) {
        Instruction existingInstruction = getInstruction(id);

        instructionMapper.updateEntityFromDto(instructionDTO, existingInstruction);

        return instructionRepository.save(existingInstruction);
    }

    @Override
    @Transactional
    public void updateLastAndNextExecutionTime(Long id, OffsetDateTime lastExecutionAt, OffsetDateTime nextExecutionAt) {

        if (id == null) {
            throw new InstructionNotFoundException("Instruction ID cannot be null");
        }

        Instruction instruction = instructionRepository.findById(id)
                .orElseThrow(() -> new InstructionNotFoundException("Instruction with ID " + id + " not found"));

        instruction.setLastExecutionAt(lastExecutionAt);
        instruction.setNextExecutionAt(nextExecutionAt);

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
        return instructionRepository.getInstructionsByInstructionStatusAndNextExecutionAtBefore(InstructionStatus.ACTIVE, OffsetDateTime.now(clock));
    }
}

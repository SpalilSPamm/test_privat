package com.example.regular_payment.controllers;

import com.example.regular_payment.dtos.InstructionCreateDTO;
import com.example.regular_payment.dtos.InstructionDTO;
import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.services.InstructionService;
import com.example.regular_payment.utils.mappers.InstructionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructions")
public class InstructionController {


    private final InstructionService instructionService;
    private final InstructionMapper instructionMapper;

    @Autowired
    public InstructionController(InstructionService instructionService, InstructionMapper instructionMapper) {
        this.instructionService = instructionService;
        this.instructionMapper = instructionMapper;
    }

    @PostMapping
    public ResponseEntity<InstructionDTO> createInstruction(@RequestBody InstructionCreateDTO instructionCreateDTO) {

        Instruction instruction  = instructionMapper.toEntity(instructionCreateDTO);

        Instruction savedInstruction = instructionService.saveInstruction(instruction);

        InstructionDTO responseDto = instructionMapper.toDTO(savedInstruction);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstructionDTO> updateInstruction(@PathVariable Long id, @RequestBody InstructionDTO instructionDTO) {

        Instruction updateInstruction = instructionService.updateInstruction(id, instructionDTO);

        InstructionDTO responseDto = instructionMapper.toDTO(updateInstruction);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstruction(@PathVariable Long id) {

        instructionService.deleteInstruction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructionDTO> getInstruction(@PathVariable Long id) {

        InstructionDTO responseDto = instructionMapper.toDTO(instructionService.getInstruction(id));

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/search/iin/{iin}")
    public ResponseEntity<List<Instruction>> getInstructionsForIin(@PathVariable String iin) {

        List<Instruction> result = instructionService.getInstructionsByIin(iin);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/search/edrpou/{edrpou}")
    public ResponseEntity<List<InstructionDTO>> getInstructionsForEdrpou(@PathVariable String edrpou) {

        List<InstructionDTO> result = instructionService.getInstructionsByEdrpou(edrpou).stream().map(instructionMapper::toDTO).toList();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<InstructionDTO>> getAllActiveInstruction() {
        List<InstructionDTO> result = instructionService.getAllActiveInstructions().stream().map(instructionMapper::toDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

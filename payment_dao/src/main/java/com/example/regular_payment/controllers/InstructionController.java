package com.example.regular_payment.controllers;

import com.example.regular_payment.models.Instruction;
import com.example.regular_payment.services.InstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructions")
public class InstructionController {

    private final InstructionService instructionService;

    @Autowired
    public InstructionController(InstructionService instructionService) {
        this.instructionService = instructionService;
    }

    @PostMapping
    public ResponseEntity<Instruction> createInstruction(@RequestBody Instruction instruction) {

        Instruction savedInstruction = instructionService.saveInstruction(instruction);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedInstruction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instruction> updateInstruction(@PathVariable Long id, @RequestBody Instruction instruction) {

        Instruction updateInstruction = instructionService.updateInstruction(id, instruction);

        return ResponseEntity.status(HttpStatus.OK).body(updateInstruction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstruction(@PathVariable Long id) {

        instructionService.deleteInstruction(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instruction> getInstruction(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(instructionService.getInstruction(id));
    }

    @GetMapping("/search/iin/{iin}")
    public ResponseEntity<List<Instruction>> getInstructionsForIin(@PathVariable String iin) {

        List<Instruction> result = instructionService.getInstructionsByIin(iin);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/search/edrpou/{edrpou}")
    public ResponseEntity<List<Instruction>> getInstructionsForEdrpou(@PathVariable String edrpou) {

        List<Instruction> result = instructionService.getInstructionsByEdrpou(edrpou);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Instruction>> getAllActiveInstruction() {
        List<Instruction> result = instructionService.getAllActiveInstructions();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

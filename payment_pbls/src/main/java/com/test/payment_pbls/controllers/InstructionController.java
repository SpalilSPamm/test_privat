package com.test.payment_pbls.controllers;

import com.test.payment_pbls.dtos.InstructionValidDTO;
import com.test.payment_pbls.dtos.Instruction;
import com.test.payment_pbls.services.InstructionService;
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
    public ResponseEntity<Instruction> createInstruction(@RequestBody InstructionValidDTO instructionValidDTO) {

        Instruction savedInstruction = instructionService.createInstruction(instructionValidDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedInstruction);
    }

    @GetMapping("/payer/{iin}")
    public ResponseEntity< List<Instruction>> getInstructionsByPayerIin(@PathVariable String iin) {

        List<Instruction> result = instructionService.getInstructionsByPayerIin(iin);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/recipient/{edrpou}")
    public ResponseEntity<List<Instruction>> getInstructionsByRecipientEdrpou(@PathVariable String edrpou) {

        List<Instruction> result = instructionService.getInstructionsByRecipientEdrpou(edrpou);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<Instruction>> getScheduledInstructions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size
    ) {
        List<Instruction> result = instructionService.getScheduledInstructions(page, size);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

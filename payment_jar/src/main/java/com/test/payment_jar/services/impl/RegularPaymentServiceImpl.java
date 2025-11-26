package com.test.payment_jar.services.impl;


import com.test.payment_jar.clients.BusinessLogicClient;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.services.RegularPaymentService;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class RegularPaymentServiceImpl implements RegularPaymentService {

    private final Clock clock;
    private final BusinessLogicClient businessLogicClient;

    @Autowired
    public RegularPaymentServiceImpl(Clock clock, BusinessLogicClient businessLogicClient) {
        this.clock = clock;
        this.businessLogicClient = businessLogicClient;
    }

    @Override
    public void processPayments() {

        List<Instruction> allActiveInstruction = businessLogicClient.getAllActiveInstructions();

        for (Instruction instruction : allActiveInstruction) {
            try {

                if (OffsetDateTime.now(clock).isAfter(instruction.getNextExecutionAt())) {
                    System.out.println("Create trans with id:" + instruction.getId());
                    businessLogicClient.createTransaction(instruction);
                }
            } catch (CreationFailureException e) {
                log.error("Failed to create transaction: " + e.getMessage());
            }
        }

    }
}

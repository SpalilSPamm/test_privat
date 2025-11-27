package com.test.payment_jar.services.impl;


import com.test.payment_jar.clients.BusinessLogicClient;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.services.RegularPaymentService;
import com.test.payment_jar.utils.exceptions.CreationFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RegularPaymentServiceImpl implements RegularPaymentService {

    private final BusinessLogicClient businessLogicClient;

    @Autowired
    public RegularPaymentServiceImpl(BusinessLogicClient businessLogicClient) {
        this.businessLogicClient = businessLogicClient;
    }

    @Override
    public void processPayments() {

        List<Instruction> allActiveInstruction = businessLogicClient.getAllActiveInstructions();

        for (Instruction instruction : allActiveInstruction) {
            try {
                    log.info("Create trans with id:{}", instruction.getId());
                    businessLogicClient.createTransaction(instruction);
            } catch (CreationFailureException e) {
                log.error("Failed to create transaction: {}", e.getMessage());
            }
        }

    }
}

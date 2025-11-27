package com.test.payment_jar.services.impl;


import com.test.payment_jar.clients.BusinessLogicClient;
import com.test.payment_jar.models.Instruction;
import com.test.payment_jar.services.RegularPaymentService;
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

        int pageSize = 1000;
        int offset = 0;

        while (true) {

            List<Instruction> batch = businessLogicClient.getScheduledInstructions(offset, pageSize);

            if (batch.isEmpty()) {
                break;
            }

            log.info("Processing batch of {} instructions", batch.size());

            businessLogicClient.createTransactionsBatch(batch);

            offset += pageSize;

            if (batch.size() < pageSize) {
                break;
            }
        }
    }
}

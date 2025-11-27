package com.test.payment_jar.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegularPaymentScheduler {


    private final RegularPaymentService regularPaymentService;

    @Autowired
    public RegularPaymentScheduler(RegularPaymentService regularPaymentService) {
        this.regularPaymentService = regularPaymentService;
    }

    @Scheduled(cron = "${regular.payment.cron.expression}")
    public void runPaymentProcess() {

        log.info("Starting the debit process...");

        regularPaymentService.processPayments();

        log.info("The withdrawal process is complete.");
    }
}

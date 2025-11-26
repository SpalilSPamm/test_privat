package com.test.payment_jar.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RegularPaymentScheduler {


    private final RegularPaymentService regularPaymentService;

    @Autowired
    public RegularPaymentScheduler(RegularPaymentService regularPaymentService) {
        this.regularPaymentService = regularPaymentService;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void runPaymentProcess() {

        System.out.println("Starting the debit process...");

        regularPaymentService.processPayments();

        System.out.println("The withdrawal process is complete.");
    }
}

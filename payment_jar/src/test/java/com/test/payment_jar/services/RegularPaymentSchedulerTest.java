package com.test.payment_jar.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RegularPaymentSchedulerTest {

    @Mock
    private RegularPaymentService regularPaymentService;

    @InjectMocks
    private RegularPaymentScheduler regularPaymentScheduler;

    @Test
    void runPaymentProcess_shouldCallServiceProcessPaymentsOnce() {

        regularPaymentScheduler.runPaymentProcess();

        verify(regularPaymentService, times(1)).processPayments();
    }
}

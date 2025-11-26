package com.test.payment_pbls.services;

public interface ValidationService {

    void validatePayerIinChecksum(String iin);
    void validatePayerEdrpouChecksum(String edrpou);
}

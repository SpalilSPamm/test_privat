package com.example.regular_payment.utils.exceptions;

public class InstructionNotFoundException extends RuntimeException {
    public InstructionNotFoundException(String message) {
        super(message);
    }
}

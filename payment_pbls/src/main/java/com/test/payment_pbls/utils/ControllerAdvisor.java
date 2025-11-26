package com.test.payment_pbls.utils;


import com.test.payment_pbls.utils.exceptions.CreationFailureException;
import com.test.payment_pbls.utils.exceptions.InstructionNotFoundException;
import com.test.payment_pbls.utils.exceptions.TransactionNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(CreationFailureException.class)
    public ResponseEntity<Object> handleInstructionCreationFailureException( CreationFailureException ex ) {

        Map<String, Object> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException( ValidationException ex ) {

        Map<String, Object> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({TransactionNotFoundException.class, InstructionNotFoundException.class})
    public ResponseEntity<Object> handleIllegalArgumentException( RuntimeException ex ) {

        Map<String, Object> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }


}

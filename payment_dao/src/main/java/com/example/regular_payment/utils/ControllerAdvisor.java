package com.example.regular_payment.utils;

import com.example.regular_payment.utils.exceptions.InstructionNotFoundException;
import com.example.regular_payment.utils.exceptions.TransactionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler({TransactionNotFoundException.class, InstructionNotFoundException.class})
    public ResponseEntity<Object> handleIllegalArgumentException( RuntimeException ex ) {

        Map<String, Object> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIllegalArgumentException( DataIntegrityViolationException ex ) {

        Map<String, Object> body = new HashMap<>();

        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}

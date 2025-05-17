package com.example.BE_SportCourtBooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidation (MethodArgumentNotValidException exception){
        String msg = "";
        for(FieldError fieldError :exception.getBindingResult().getFieldErrors()){
            msg += fieldError.getDefaultMessage()+"\n";
        }
        if (msg.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleValidation (Exception exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
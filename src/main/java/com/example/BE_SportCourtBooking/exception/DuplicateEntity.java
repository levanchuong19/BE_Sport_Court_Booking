package com.example.BE_SportCourtBooking.exception;

public class DuplicateEntity extends RuntimeException {
    public DuplicateEntity(String message) {
        super(message);
    }
}

package com.example.DevNotes.exceptions;

public class PostValidationException extends RuntimeException {
    public PostValidationException(String message) {
        super(message);
    }
}
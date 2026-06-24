package com.example.DevNotes.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePostNotFound() {
        return "post-not-found";
    }

    @ExceptionHandler(PostAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handlePostAlreadyExists() {
        return "new-post";
    }
}
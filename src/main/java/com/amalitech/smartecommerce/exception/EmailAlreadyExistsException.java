package com.amalitech.smartecommerce.exception;

public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}


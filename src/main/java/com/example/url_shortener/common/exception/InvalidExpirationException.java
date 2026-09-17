package com.example.url_shortener.common.exception;

public class InvalidExpirationException extends RuntimeException {
    public InvalidExpirationException(String message) {
        super(message);
    }
}
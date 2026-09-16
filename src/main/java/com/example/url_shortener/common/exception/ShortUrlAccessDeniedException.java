package com.example.url_shortener.common.exception;

public class ShortUrlAccessDeniedException extends RuntimeException {
    public ShortUrlAccessDeniedException(String message) {
        super(message);
    }
}
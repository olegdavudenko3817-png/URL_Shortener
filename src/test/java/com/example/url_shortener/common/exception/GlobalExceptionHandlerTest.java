package com.example.url_shortener.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.example.url_shortener.common.exception.InvalidUrlException;
import com.example.url_shortener.common.exception.InvalidExpirationException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleShortUrlNotFound() {
        ShortUrlNotFoundException exception = new ShortUrlNotFoundException("Short URL not found");
        ResponseEntity<Map<String, String>> response = handler.handleShortUrlNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Short URL not found", response.getBody().get("error"));
    }

    @Test
    void shouldHandleAccessDenied() {
        ShortUrlAccessDeniedException exception = new ShortUrlAccessDeniedException("Access denied");
        ResponseEntity<Map<String, String>> response = handler.handleAccessDenied(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("error"));
    }

    @Test
    void shouldHandleExpiredUrl() {
        ShortUrlExpiredException exception = new ShortUrlExpiredException("Short URL has expired");
        ResponseEntity<Map<String, String>> response = handler.handleExpired(exception);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Short URL has expired", response.getBody().get("error"));
    }

    @Test
    void shouldHandleDuplicateUsername() {
        UsernameAlreadyExistsException exception = new UsernameAlreadyExistsException("Username already exists");
        ResponseEntity<Map<String, String>> response = handler.handleUsernameAlreadyExists(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already exists", response.getBody().get("error"));
    }

    @Test
    void shouldHandleInvalidCredentials() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidCredentials(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().get("error"));
    }

    @Test
    void shouldHandleAuthenticationRequired() {
        AuthenticationRequiredException exception = new AuthenticationRequiredException("User is not authenticated");
        ResponseEntity<Map<String, String>> response = handler.handleAuthenticationRequired(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not authenticated", response.getBody().get("error"));
    }

    @Test
    void shouldHandleValidationError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("request", "username", "username must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("username must not be blank", response.getBody().get("username"));
    }

    @Test
    void shouldHandleInvalidUrl() {
        InvalidUrlException exception = new InvalidUrlException("Invalid URL");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidUrl(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid URL", response.getBody().get("error"));
    }

    @Test
    void shouldHandleInvalidExpiration() {
        InvalidExpirationException exception = new InvalidExpirationException("Expiration date must be in the future");
        ResponseEntity<Map<String, String>> response = handler.handleInvalidExpiration(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Expiration date must be in the future", response.getBody().get("error"));
    }
}
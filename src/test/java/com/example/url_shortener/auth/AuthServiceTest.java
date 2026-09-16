package com.example.url_shortener.auth;

import com.example.url_shortener.auth.dto.AuthResponse;
import com.example.url_shortener.auth.dto.LoginRequest;
import com.example.url_shortener.common.exception.InvalidCredentialsException;
import com.example.url_shortener.user.User;
import com.example.url_shortener.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private User user;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldAuthenticateUser() {
        LoginRequest request = new LoginRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("encoded-password");
        when(passwordEncoder.matches("Test12345", "encoded-password")).thenReturn(true);

        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse result = authService.authenticate(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("Test12345", "encoded-password");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectUnknownUsername() {
        LoginRequest request = new LoginRequest();

        request.setUsername("unknown");
        request.setPassword("Test12345");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(request));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();

        request.setUsername("testuser");
        request.setPassword("Wrong12345");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("encoded-password");
        when(passwordEncoder.matches("Wrong12345", "encoded-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(request));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(passwordEncoder).matches("Wrong12345", "encoded-password");
    }
}
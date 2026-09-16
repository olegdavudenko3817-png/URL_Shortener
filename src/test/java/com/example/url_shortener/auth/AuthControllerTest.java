package com.example.url_shortener.auth;

import com.example.url_shortener.auth.dto.AuthResponse;
import com.example.url_shortener.auth.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void shouldLoginUser() {
        LoginRequest request = new LoginRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        AuthResponse authResponse = new AuthResponse("jwt-token");

        when(authService.authenticate(request))
                .thenReturn(authResponse);

        AuthResponse response = authController.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(authService).authenticate(request);
    }
}
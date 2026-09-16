package com.example.url_shortener.user;

import com.example.url_shortener.user.dto.RegistrationRequest;
import com.example.url_shortener.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private User user;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void shouldRegisterUser() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        UserResponse userResponse = new UserResponse(1L, "testuser", OffsetDateTime.parse("2026-09-15T12:00:00Z"));

        when(userService.register(request)).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = userController.register(request);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(userResponse, response.getBody());

        verify(userService).register(request);
    }

    @Test
    void shouldReturnCurrentUser() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-15T12:00:00Z");

        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getCreatedAt()).thenReturn(createdAt);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));

        UserResponse response = userController.me();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
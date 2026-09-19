package com.example.url_shortener.user;

import com.example.url_shortener.common.exception.UsernameAlreadyExistsException;
import com.example.url_shortener.user.dto.RegistrationRequest;
import com.example.url_shortener.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private User savedUser;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserWithEncodedPassword() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("Test12345")).thenReturn("encoded-password");
        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getUsername()).thenReturn("testuser");

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-15T12:00:00Z");
        when(savedUser.getCreatedAt()).thenReturn(createdAt);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.saveAndFlush(userCaptor.capture())).thenReturn(savedUser);

        UserResponse result = userService.register(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(createdAt, result.getCreatedAt());

        User userToSave = userCaptor.getValue();

        assertEquals("testuser", userToSave.getUsername());
        assertEquals("encoded-password", userToSave.getPassword());

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("Test12345");
        verify(userRepository).saveAndFlush(userToSave);
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(request));

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode("Test12345");
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("anotheruser");
        request.setPassword("Secure123");

        when(userRepository.existsByUsername("anotheruser")).thenReturn(false);

        when(passwordEncoder.encode("Secure123")).thenReturn("encoded-secure-password");

        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User saved = userCaptor.getValue();

        assertEquals("anotheruser", saved.getUsername());
        assertEquals("encoded-secure-password", saved.getPassword());

        verify(passwordEncoder).encode("Secure123");
    }

    @Test
    void shouldNotSaveUserWhenUsernameExists() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("existing");
        request.setPassword("Test12345");

        when(userRepository.existsByUsername("existing")).thenReturn(true);
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).saveAndFlush(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowUsernameAlreadyExistsWhenDatabaseReportsUsernameConflict() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("concurrent");
        request.setPassword("Test12345");

        when(userRepository.existsByUsername("concurrent")).thenReturn(false);
        when(passwordEncoder.encode("Test12345")).thenReturn("encoded-password");

        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uq_users_username\"");

        when(userRepository.saveAndFlush(any(User.class))).thenThrow(exception);
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(request));

        verify(userRepository).existsByUsername("concurrent");
        verify(passwordEncoder).encode("Test12345");
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void shouldRethrowUnexpectedDataIntegrityViolation() {
        RegistrationRequest request = new RegistrationRequest();

        request.setUsername("testuser");
        request.setPassword("Test12345");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("Test12345")).thenReturn("encoded-password");
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "some other database constraint violation");

        when(userRepository.saveAndFlush(any(User.class))).thenThrow(exception);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                        () -> userService.register(request));

        assertEquals(exception, thrown);
        verify(userRepository).saveAndFlush(any(User.class));
    }
}

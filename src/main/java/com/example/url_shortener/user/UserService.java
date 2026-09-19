package com.example.url_shortener.user;

import com.example.url_shortener.common.exception.UsernameAlreadyExistsException;
import com.example.url_shortener.user.dto.RegistrationRequest;
import com.example.url_shortener.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegistrationRequest registrationRequest) {
        String username = registrationRequest.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        User user;

        try {
            user = userRepository.saveAndFlush(new User(username, encodedPassword));
        } catch (DataIntegrityViolationException exception) {
            if (isUsernameConflict(exception)) {
                throw new UsernameAlreadyExistsException("Username already exists");
            }
            throw exception;
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    private boolean isUsernameConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("uq_users_username")) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }
}
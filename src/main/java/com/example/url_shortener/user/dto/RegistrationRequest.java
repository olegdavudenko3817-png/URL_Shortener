package com.example.url_shortener.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "username cannot be empty")
    @Size(max = 100, message = "username cannot exceed 100 characters")
    private String username;

    @NotBlank(message = "password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "password must contain lowercase, uppercase letter and digit")
    @Size(min = 8, max = 255, message = "password must contain between 8 and 255 characters")
    private String password;
}

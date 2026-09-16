package com.example.url_shortener.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    Long id;
    String username;
    OffsetDateTime createdAt;
}

package com.example.hackaton1.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long expiresIn;
    private String role;
    private String branch;

    private String id;
    private String username;
    private String email;
    private Instant createdAt;
}
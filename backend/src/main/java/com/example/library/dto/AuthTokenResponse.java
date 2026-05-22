package com.example.library.dto;

import com.example.library.model.UserRole;
import java.time.Instant;

public record AuthTokenResponse(
    String accessToken,
    String tokenType,
    Instant expiresAt,
    Long userId,
    String email,
    String fullName,
    UserRole role) {}

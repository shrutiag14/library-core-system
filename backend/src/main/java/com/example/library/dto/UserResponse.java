package com.example.library.dto;

import com.example.library.model.AppUser;
import com.example.library.model.UserRole;
import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    UserRole role,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt) {
  public static UserResponse from(AppUser user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.isEnabled(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}

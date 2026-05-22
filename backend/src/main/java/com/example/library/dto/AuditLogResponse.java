package com.example.library.dto;

import com.example.library.model.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String actorEmail,
    String action,
    String entityType,
    String entityId,
    String details,
    Instant createdAt) {
  public static AuditLogResponse from(AuditLog log) {
    return new AuditLogResponse(
        log.getId(),
        log.getActorEmail(),
        log.getAction(),
        log.getEntityType(),
        log.getEntityId(),
        log.getDetails(),
        log.getCreatedAt());
  }
}

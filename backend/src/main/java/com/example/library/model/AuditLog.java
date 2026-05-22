package com.example.library.model;

import com.example.library.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {
  @Column(nullable = false)
  private String actorEmail;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(nullable = false, length = 64)
  private String entityType;

  @Column(length = 64)
  private String entityId;

  @Column(length = 1000)
  private String details;
}

package com.example.library.service;

import com.example.library.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
  void record(String action, String entityType, Object entityId, String details);

  Page<AuditLogResponse> list(Pageable pageable);
}

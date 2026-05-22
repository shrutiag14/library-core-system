package com.example.library.service.impl;

import com.example.library.dto.AuditLogResponse;
import com.example.library.model.AuditLog;
import com.example.library.repository.AuditLogRepository;
import com.example.library.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogRepository repository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(String action, String entityType, Object entityId, String details) {
    AuditLog log = new AuditLog();
    log.setActorEmail(currentActor());
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId == null ? null : entityId.toString());
    log.setDetails(details);
    repository.save(log);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AuditLogResponse> list(Pageable pageable) {
    return repository.findAll(pageable).map(AuditLogResponse::from);
  }

  private String currentActor() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      return "system";
    }
    return authentication.getName();
  }
}

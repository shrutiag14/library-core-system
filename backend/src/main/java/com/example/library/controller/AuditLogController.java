package com.example.library.controller;

import com.example.library.dto.AuditLogResponse;
import com.example.library.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
  private final AuditLogService service;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Page<AuditLogResponse> list(Pageable pageable) {
    return service.list(pageable);
  }
}

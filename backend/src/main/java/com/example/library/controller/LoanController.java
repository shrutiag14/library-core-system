package com.example.library.controller;

import com.example.library.dto.DashboardResponse;
import com.example.library.dto.IssueRequest;
import com.example.library.dto.LoanResponse;
import com.example.library.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class LoanController {
  private final LoanService service;

  @GetMapping("/dashboard")
  public DashboardResponse dashboard() {
    return service.dashboard();
  }

  @PostMapping("/loans")
  @ResponseStatus(HttpStatus.CREATED)
  public LoanResponse issue(@Valid @RequestBody IssueRequest request) {
    return service.issue(request);
  }

  @PatchMapping("/loans/{id}/return")
  public LoanResponse returnLoan(@PathVariable Long id) {
    return service.returnLoan(id);
  }

  @GetMapping("/loans")
  public Page<LoanResponse> history(
      @RequestParam(required = false) Long memberId,
      @RequestParam(required = false) Long bookId,
      Pageable pageable) {
    return service.history(memberId, bookId, pageable);
  }
}

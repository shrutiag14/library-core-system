package com.example.library.service;

import com.example.library.dto.DashboardResponse;
import com.example.library.dto.IssueRequest;
import com.example.library.dto.LoanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {
  LoanResponse issue(IssueRequest request);

  LoanResponse returnLoan(Long id);

  Page<LoanResponse> history(Long memberId, Long bookId, Pageable pageable);

  DashboardResponse dashboard();
}

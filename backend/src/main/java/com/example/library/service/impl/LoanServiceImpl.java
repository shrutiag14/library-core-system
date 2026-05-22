package com.example.library.service.impl;

import com.example.library.dto.DashboardResponse;
import com.example.library.dto.IssueRequest;
import com.example.library.dto.LoanResponse;
import com.example.library.config.AppProperties;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.ConflictException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import com.example.library.model.Member;
import com.example.library.model.MemberStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.LoanService;
import com.example.library.service.AuditLogService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
  private final LoanRepository loanRepository;
  private final BookRepository bookRepository;
  private final MemberRepository memberRepository;
  private final AppProperties properties;
  private final AuditLogService auditLogService;

  @Override
  @Transactional
  public LoanResponse issue(IssueRequest request) {
    Book book = bookRepository.findActiveByIdForUpdate(request.bookId())
        .orElseThrow(() -> new NotFoundException("Book not found"));
    Member member = memberRepository.findByIdAndStatusForUpdate(request.memberId(), MemberStatus.ACTIVE)
        .orElseThrow(() -> new NotFoundException("Active member not found"));
    if (loanRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), member.getId(), LoanStatus.ISSUED)) {
      throw new ConflictException("Member already has this book issued");
    }
    if (book.getAvailableCopies() <= 0) {
      throw new BadRequestException("Book is not available");
    }
    book.setAvailableCopies(book.getAvailableCopies() - 1);
    Loan loan = new Loan();
    loan.setBook(book);
    loan.setMember(member);
    loan.setIssuedAt(Instant.now());
    loan.setDueDate(request.dueDate() == null
        ? LocalDate.now().plusDays(properties.library().defaultLoanDays())
        : request.dueDate());
    loan.setStatus(LoanStatus.ISSUED);
    Loan saved = loanRepository.save(loan);
    auditLogService.record("ISSUE_LOAN", "Loan", saved.getId(),
        "bookId=%s,memberId=%s,dueDate=%s".formatted(book.getId(), member.getId(), saved.getDueDate()));
    return LoanResponse.from(saved, finePerDay());
  }

  @Override
  @Transactional
  public LoanResponse returnLoan(Long id) {
    Loan loan = loanRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Loan not found"));
    if (loan.getStatus() == LoanStatus.RETURNED || loan.getReturnedAt() != null) {
      throw new BadRequestException("Loan already returned");
    }
    Book book = loan.getBook();
    book.setAvailableCopies(book.getAvailableCopies() + 1);
    loan.setReturnedAt(Instant.now());
    loan.setStatus(LoanStatus.RETURNED);
    auditLogService.record("RETURN_LOAN", "Loan", loan.getId(),
        "bookId=%s,memberId=%s,fine=%s".formatted(
            book.getId(), loan.getMember().getId(), LoanResponse.from(loan, finePerDay()).fineAmount()));
    return LoanResponse.from(loan, finePerDay());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LoanResponse> history(Long memberId, Long bookId, Pageable pageable) {
    if (memberId != null) {
      ensureMemberExists(memberId);
      return loanRepository.findByMemberId(memberId, pageable).map(loan -> LoanResponse.from(loan, finePerDay()));
    }
    if (bookId != null) {
      ensureBookExists(bookId);
      return loanRepository.findByBookId(bookId, pageable).map(loan -> LoanResponse.from(loan, finePerDay()));
    }
    return loanRepository.findAll(pageable).map(loan -> LoanResponse.from(loan, finePerDay()));
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardResponse dashboard() {
    LocalDate today = LocalDate.now();
    return new DashboardResponse(
        bookRepository.countByDeletedFalse(),
        memberRepository.count(),
        loanRepository.countByStatus(LoanStatus.ISSUED),
        loanRepository.countOverdue(today),
        outstandingFines(today));
  }

  private BigDecimal outstandingFines(LocalDate today) {
    return loanRepository.findActiveOverdue(today).stream()
        .map(loan -> finePerDay().multiply(BigDecimal.valueOf(
            Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), today)))))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal finePerDay() {
    return properties.library().overdueFinePerDay() == null
        ? BigDecimal.ZERO
        : properties.library().overdueFinePerDay();
  }

  private void ensureMemberExists(Long id) {
    if (!memberRepository.existsById(id)) {
      throw new NotFoundException("Member not found");
    }
  }

  private void ensureBookExists(Long id) {
    if (!bookRepository.existsById(id)) {
      throw new NotFoundException("Book not found");
    }
  }
}

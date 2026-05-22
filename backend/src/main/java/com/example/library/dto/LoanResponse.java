package com.example.library.dto;

import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LoanResponse(
    Long id,
    Long bookId,
    String bookTitle,
    String bookIsbn,
    Long memberId,
    String memberName,
    String memberEmail,
    Instant issuedAt,
    LocalDate dueDate,
    Instant returnedAt,
    LoanStatus status,
    long overdueDays,
    BigDecimal fineAmount) {
  public static LoanResponse from(Loan loan, BigDecimal finePerDay) {
    return from(loan, finePerDay, Clock.systemDefaultZone());
  }

  public static LoanResponse from(Loan loan, BigDecimal finePerDay, Clock clock) {
    LocalDate today = LocalDate.now(clock);
    LocalDate endDate = loan.getReturnedAt() == null
        ? today
        : LocalDate.ofInstant(loan.getReturnedAt(), clock.getZone());
    long overdueDays = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), endDate));
    BigDecimal fineAmount = finePerDay.multiply(BigDecimal.valueOf(overdueDays));
    LoanStatus currentStatus = loan.getStatus();
    if (currentStatus == LoanStatus.ISSUED && overdueDays > 0) {
      currentStatus = LoanStatus.OVERDUE;
    }
    return new LoanResponse(
        loan.getId(),
        loan.getBook().getId(),
        loan.getBook().getTitle(),
        loan.getBook().getIsbn(),
        loan.getMember().getId(),
        loan.getMember().getName(),
        loan.getMember().getEmail(),
        loan.getIssuedAt(),
        loan.getDueDate(),
        loan.getReturnedAt(),
        currentStatus,
        overdueDays,
        fineAmount);
  }
}

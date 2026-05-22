package com.example.library.loan;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.library.dto.LoanResponse;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import com.example.library.model.Member;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoanResponseTest {
  @Test
  void calculatesOverdueFineForActiveLoan() {
    Book book = new Book();
    book.setTitle("Clean Code");
    book.setIsbn("9780132350884");
    Member member = new Member();
    member.setName("Ada Lovelace");
    member.setEmail("ada@example.com");
    Loan loan = new Loan();
    loan.setBook(book);
    loan.setMember(member);
    loan.setIssuedAt(Instant.parse("2026-05-01T00:00:00Z"));
    loan.setDueDate(LocalDate.parse("2026-05-10"));
    loan.setStatus(LoanStatus.ISSUED);
    Clock clock = Clock.fixed(Instant.parse("2026-05-13T00:00:00Z"), ZoneOffset.UTC);

    LoanResponse response = LoanResponse.from(loan, new BigDecimal("5.00"), clock);

    assertThat(response.status()).isEqualTo(LoanStatus.OVERDUE);
    assertThat(response.overdueDays()).isEqualTo(3);
    assertThat(response.fineAmount()).isEqualByComparingTo("15.00");
  }
}

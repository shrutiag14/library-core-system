package com.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.library.dto.BookRequest;
import com.example.library.dto.IssueRequest;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.ConflictException;
import com.example.library.dto.MemberRequest;
import com.example.library.model.LoanStatus;
import com.example.library.model.MemberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LoanWorkflowIntegrationTest {
  @Autowired private BookService bookService;
  @Autowired private MemberService memberService;
  @Autowired private LoanService loanService;

  @Test
  void issueAndReturnUpdatesStock() {
    var book = bookService.create(new BookRequest("Clean Code", "Robert Martin", "9780132350884", "Software", 2, 2, "A1"));
    var member = memberService.create(new MemberRequest("Ada Lovelace", "ada@example.com", MemberStatus.ACTIVE));

    var loan = loanService.issue(new IssueRequest(book.id(), member.id(), null));
    assertThat(loan.status()).isEqualTo(LoanStatus.ISSUED);
    assertThat(bookService.get(book.id()).availableCopies()).isEqualTo(1);

    var returned = loanService.returnLoan(loan.id());
    assertThat(returned.status()).isEqualTo(LoanStatus.RETURNED);
    assertThat(bookService.get(book.id()).availableCopies()).isEqualTo(2);
  }

  @Test
  void memberCannotIssueSameBookTwice() {
    var book = bookService.create(new BookRequest("Domain-Driven Design", "Eric Evans", "9780321125217", "Software", 1, 1, "A2"));
    var member = memberService.create(new MemberRequest("Grace Hopper", "grace@example.com", MemberStatus.ACTIVE));

    loanService.issue(new IssueRequest(book.id(), member.id(), null));

    assertThatThrownBy(() -> loanService.issue(new IssueRequest(book.id(), member.id(), null)))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("already has this book");
  }

  @Test
  void unavailableBookCannotBeIssued() {
    var book = bookService.create(new BookRequest("Refactoring", "Martin Fowler", "9780201485677", "Software", 1, 0, "A3"));
    var member = memberService.create(new MemberRequest("Katherine Johnson", "katherine@example.com", MemberStatus.ACTIVE));

    assertThatThrownBy(() -> loanService.issue(new IssueRequest(book.id(), member.id(), null)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("not available");
  }
}

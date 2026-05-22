package com.example.library.repository;

import com.example.library.model.Loan;
import com.example.library.model.LoanStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {
  boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, LoanStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"book", "member"})
  @Query("select l from Loan l where l.id = :id")
  Optional<Loan> findByIdForUpdate(@Param("id") Long id);

  @EntityGraph(attributePaths = {"book", "member"})
  Page<Loan> findByBookId(Long bookId, Pageable pageable);

  @EntityGraph(attributePaths = {"book", "member"})
  Page<Loan> findByMemberId(Long memberId, Pageable pageable);

  @EntityGraph(attributePaths = {"book", "member"})
  Page<Loan> findAll(Pageable pageable);

  long countByStatus(LoanStatus status);

  @Query("select count(l) from Loan l where l.status = 'ISSUED' and l.dueDate < :today")
  long countOverdue(LocalDate today);

  @Query("select l from Loan l where l.status = 'ISSUED' and l.dueDate < :today")
  java.util.List<Loan> findActiveOverdue(LocalDate today);
}

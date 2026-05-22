package com.example.library.repository;

import com.example.library.model.Member;
import com.example.library.model.MemberStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
  boolean existsByEmail(String email);
  boolean existsByEmailAndIdNot(String email, Long id);
  Optional<Member> findByIdAndStatus(Long id, MemberStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Member m where m.id = :id and m.status = :status")
  Optional<Member> findByIdAndStatusForUpdate(@Param("id") Long id, @Param("status") MemberStatus status);

  Page<Member> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}

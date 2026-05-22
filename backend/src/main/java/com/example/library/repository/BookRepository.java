package com.example.library.repository;

import com.example.library.model.Book;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
  Optional<Book> findByIdAndDeletedFalse(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Book b where b.id = :id and b.deleted = false")
  Optional<Book> findActiveByIdForUpdate(@Param("id") Long id);

  boolean existsByIsbnAndDeletedFalse(String isbn);
  boolean existsByIsbnAndIdNotAndDeletedFalse(String isbn, Long id);
  long countByDeletedFalse();
}

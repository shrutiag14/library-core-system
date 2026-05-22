package com.example.library.specification;

import com.example.library.model.Book;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {
  private BookSpecifications() {}

  public static Specification<Book> active() {
    return (root, query, cb) -> cb.isFalse(root.get("deleted"));
  }

  public static Specification<Book> search(String term) {
    return (root, query, cb) -> {
      if (term == null || term.isBlank()) {
        return cb.conjunction();
      }
      String like = "%" + term.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), like),
          cb.like(cb.lower(root.get("author")), like),
          cb.like(cb.lower(root.get("isbn")), like),
          cb.like(cb.lower(root.get("category")), like));
    };
  }
}

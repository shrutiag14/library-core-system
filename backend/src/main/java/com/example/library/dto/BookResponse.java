package com.example.library.dto;

import com.example.library.model.Book;
import java.time.Instant;

public record BookResponse(
    Long id,
    String title,
    String author,
    String isbn,
    String category,
    int totalCopies,
    int availableCopies,
    String shelfLocation,
    Instant createdAt,
    Instant updatedAt) {
  public static BookResponse from(Book book) {
    return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
        book.getCategory(), book.getTotalCopies(), book.getAvailableCopies(),
        book.getShelfLocation(), book.getCreatedAt(), book.getUpdatedAt());
  }
}

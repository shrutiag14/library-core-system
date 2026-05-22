package com.example.library.service.impl;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.ConflictException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import com.example.library.service.AuditLogService;
import com.example.library.service.BookService;
import com.example.library.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
  private final BookRepository repository;
  private final AuditLogService auditLogService;

  @Override
  @Transactional(readOnly = true)
  public Page<BookResponse> list(String search, Pageable pageable) {
    return repository.findAll(BookSpecifications.active().and(BookSpecifications.search(search)), pageable)
        .map(BookResponse::from);
  }

  @Override
  @Transactional(readOnly = true)
  public Book getActive(Long id) {
    return repository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("Book not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public BookResponse get(Long id) {
    return BookResponse.from(getActive(id));
  }

  @Override
  @Transactional
  public BookResponse create(BookRequest request) {
    validateCopies(request.totalCopies(), request.availableCopies());
    if (repository.existsByIsbnAndDeletedFalse(request.isbn())) {
      throw new ConflictException("ISBN already exists");
    }
    Book book = new Book();
    apply(book, request);
    Book saved = repository.save(book);
    auditLogService.record("CREATE_BOOK", "Book", saved.getId(), saved.getIsbn());
    return BookResponse.from(saved);
  }

  @Override
  @Transactional
  public BookResponse update(Long id, BookRequest request) {
    validateCopies(request.totalCopies(), request.availableCopies());
    if (repository.existsByIsbnAndIdNotAndDeletedFalse(request.isbn(), id)) {
      throw new ConflictException("ISBN already exists");
    }
    Book book = getActive(id);
    int issuedCopies = book.getTotalCopies() - book.getAvailableCopies();
    if (request.totalCopies() < issuedCopies) {
      throw new BadRequestException("Total copies cannot be less than issued copies");
    }
    apply(book, request);
    auditLogService.record("UPDATE_BOOK", "Book", book.getId(), book.getIsbn());
    return BookResponse.from(book);
  }

  @Override
  @Transactional
  public void softDelete(Long id) {
    Book book = getActive(id);
    if (book.getTotalCopies() != book.getAvailableCopies()) {
      throw new BadRequestException("Cannot delete book with active loans");
    }
    book.setDeleted(true);
    auditLogService.record("DELETE_BOOK", "Book", book.getId(), book.getIsbn());
  }

  private void apply(Book book, BookRequest request) {
    book.setTitle(request.title().trim());
    book.setAuthor(request.author().trim());
    book.setIsbn(request.isbn().trim());
    book.setCategory(blankToNull(request.category()));
    book.setTotalCopies(request.totalCopies());
    book.setAvailableCopies(request.availableCopies());
    book.setShelfLocation(blankToNull(request.shelfLocation()));
  }

  private void validateCopies(int total, int available) {
    if (available > total) {
      throw new BadRequestException("Available copies cannot exceed total copies");
    }
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}

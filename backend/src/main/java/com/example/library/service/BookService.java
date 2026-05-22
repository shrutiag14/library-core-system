package com.example.library.service;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
  Page<BookResponse> list(String search, Pageable pageable);

  Book getActive(Long id);

  BookResponse get(Long id);

  BookResponse create(BookRequest request);

  BookResponse update(Long id, BookRequest request);

  void softDelete(Long id);
}

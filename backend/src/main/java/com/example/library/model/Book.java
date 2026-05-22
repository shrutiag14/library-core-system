package com.example.library.model;

import com.example.library.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book extends BaseEntity {
  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false, unique = true, length = 32)
  private String isbn;

  private String category;
  private int totalCopies;
  private int availableCopies;
  private String shelfLocation;
  private boolean deleted;
}

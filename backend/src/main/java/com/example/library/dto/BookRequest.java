package com.example.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String author,
    @NotBlank @Size(max = 32) String isbn,
    @Size(max = 120) String category,
    @Min(0) int totalCopies,
    @Min(0) int availableCopies,
    @Size(max = 120) String shelfLocation) {}

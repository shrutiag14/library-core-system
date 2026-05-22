package com.example.library.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record IssueRequest(
    @NotNull Long bookId,
    @NotNull Long memberId,
    @FutureOrPresent LocalDate dueDate) {}

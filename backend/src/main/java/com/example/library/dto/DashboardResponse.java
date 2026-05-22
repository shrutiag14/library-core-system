package com.example.library.dto;

import java.math.BigDecimal;

public record DashboardResponse(
    long books,
    long members,
    long activeLoans,
    long overdueLoans,
    BigDecimal outstandingFines) {}

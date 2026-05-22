package com.example.library.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid Cors cors, @Valid Library library, @Valid Security security) {
  public record Cors(@NotEmpty List<String> allowedOrigins) {}
  public record Library(@Min(1) int defaultLoanDays, BigDecimal overdueFinePerDay) {}
  public record Security(@Valid Jwt jwt) {}
  public record Jwt(@NotBlank String secret, @Min(1) int expirationMinutes) {}
}

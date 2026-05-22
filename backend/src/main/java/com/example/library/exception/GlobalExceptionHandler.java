package com.example.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
  }

  @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
  ResponseEntity<ApiError> badRequest(RuntimeException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ApiError> unauthorized(AuthenticationException ex, HttpServletRequest request) {
    return error(HttpStatus.UNAUTHORIZED, "Invalid credentials", request, Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      fields.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return error(HttpStatus.BAD_REQUEST, "Validation failed", request, fields);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "Unique or relationship constraint violated", request, Map.of());
  }

  @ExceptionHandler({
      ObjectOptimisticLockingFailureException.class,
      OptimisticLockException.class,
      PessimisticLockingFailureException.class
  })
  ResponseEntity<ApiError> concurrency(RuntimeException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "Resource was updated by another transaction; retry request", request, Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled API error at {}", request.getRequestURI(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, Map.of());
  }

  private ResponseEntity<ApiError> error(
      HttpStatus status, String message, HttpServletRequest request, Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
            request.getRequestURI(), fields));
  }
}

package com.example.library.controller;

import com.example.library.dto.AuthTokenResponse;
import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.UserResponse;
import com.example.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
    return service.login(request);
  }

  @PostMapping("/bootstrap")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse bootstrapAdmin(@Valid @RequestBody CreateUserRequest request) {
    return service.bootstrapAdmin(request);
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return service.createUser(request);
  }
}

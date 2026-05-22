package com.example.library.service;

import com.example.library.dto.AuthTokenResponse;
import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.UserResponse;

public interface AuthService {
  AuthTokenResponse login(LoginRequest request);

  UserResponse bootstrapAdmin(CreateUserRequest request);

  UserResponse createUser(CreateUserRequest request);
}

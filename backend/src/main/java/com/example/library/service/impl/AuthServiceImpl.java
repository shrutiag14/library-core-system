package com.example.library.service.impl;

import com.example.library.dto.AuthTokenResponse;
import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.UserResponse;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.ConflictException;
import com.example.library.model.AppUser;
import com.example.library.model.UserRole;
import com.example.library.repository.AppUserRepository;
import com.example.library.security.AuthenticatedUser;
import com.example.library.security.JwtService;
import com.example.library.service.AuditLogService;
import com.example.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final AppUserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuditLogService auditLogService;

  @Override
  @Transactional(readOnly = true)
  public AuthTokenResponse login(LoginRequest request) {
    var authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email().trim().toLowerCase(),
            request.password()));
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    return new AuthTokenResponse(
        jwtService.generateToken(user),
        "Bearer",
        jwtService.expiresAt(),
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getAuthorities().stream()
            .map(Object::toString)
            .findFirst()
            .map(role -> UserRole.valueOf(role.replace("ROLE_", "")))
            .orElse(UserRole.LIBRARIAN));
  }

  @Override
  @Transactional
  public UserResponse bootstrapAdmin(CreateUserRequest request) {
    if (repository.count() > 0) {
      throw new ConflictException("Bootstrap is allowed only before any users exist");
    }
    if (request.role() != UserRole.ADMIN) {
      throw new BadRequestException("Bootstrap user must be ADMIN");
    }
    UserResponse user = create(request);
    auditLogService.record("BOOTSTRAP_ADMIN", "AppUser", user.id(), user.email());
    return user;
  }

  @Override
  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    UserResponse user = create(request);
    auditLogService.record("CREATE_USER", "AppUser", user.id(), user.email() + ":" + user.role());
    return user;
  }

  private UserResponse create(CreateUserRequest request) {
    String email = request.email().trim().toLowerCase();
    if (repository.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("User email already exists");
    }
    AppUser user = new AppUser();
    user.setFullName(request.fullName().trim());
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(request.role());
    user.setEnabled(true);
    return UserResponse.from(repository.save(user));
  }
}

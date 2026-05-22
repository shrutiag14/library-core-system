package com.example.library.security;

import com.example.library.model.AppUser;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthenticatedUser implements UserDetails {
  private final Long id;
  private final String email;
  private final String fullName;
  private final String password;
  private final boolean enabled;
  private final Collection<? extends GrantedAuthority> authorities;

  public AuthenticatedUser(AppUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.fullName = user.getFullName();
    this.password = user.getPasswordHash();
    this.enabled = user.isEnabled();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getUsername() {
    return email;
  }
}

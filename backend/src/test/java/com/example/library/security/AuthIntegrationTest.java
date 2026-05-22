package com.example.library.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.library.dto.AuthTokenResponse;
import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.LoginRequest;
import com.example.library.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void protectedApiRequiresToken() throws Exception {
    mockMvc.perform(get("/api/dashboard"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void bootstrapAdminThenLoginThenAccessProtectedApi() throws Exception {
    var request = new CreateUserRequest("Admin User", "admin@example.com", "password123", UserRole.ADMIN);

    mockMvc.perform(post("/api/auth/bootstrap")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    String loginResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "password123"))))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    AuthTokenResponse token = objectMapper.readValue(loginResponse, AuthTokenResponse.class);
    assertThat(token.accessToken()).isNotBlank();

    mockMvc.perform(get("/api/dashboard")
            .header("Authorization", "Bearer " + token.accessToken()))
        .andExpect(status().isOk());
  }
}

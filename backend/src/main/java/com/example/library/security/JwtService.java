package com.example.library.security;

import com.example.library.config.AppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
  private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

  private final AppProperties properties;
  private final ObjectMapper objectMapper;

  public String generateToken(AuthenticatedUser user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(properties.security().jwt().expirationMinutes() * 60L);
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", user.getUsername());
    claims.put("uid", user.getId());
    claims.put("name", user.getFullName());
    claims.put("roles", user.getAuthorities().stream().map(Object::toString).toList());
    claims.put("iat", now.getEpochSecond());
    claims.put("exp", expiresAt.getEpochSecond());
    return sign(claims);
  }

  public Instant expiresAt() {
    return Instant.now().plusSeconds(properties.security().jwt().expirationMinutes() * 60L);
  }

  public String extractSubject(String token) {
    Object subject = claims(token).get("sub");
    return subject == null ? null : subject.toString();
  }

  public boolean isValid(String token, AuthenticatedUser user) {
    Map<String, Object> claims = claims(token);
    Object subject = claims.get("sub");
    Object expiry = claims.get("exp");
    return subject != null
        && subject.equals(user.getUsername())
        && expiry instanceof Number number
        && Instant.now().isBefore(Instant.ofEpochSecond(number.longValue()))
        && sign(claims).equals(token);
  }

  private String sign(Map<String, Object> claims) {
    try {
      String header = URL_ENCODER.encodeToString(
          objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      String payload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(claims));
      String unsigned = header + "." + payload;
      return unsigned + "." + hmac(unsigned);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to create JWT", ex);
    }
  }

  private Map<String, Object> claims(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid JWT");
      }
      String unsigned = parts[0] + "." + parts[1];
      if (!hmac(unsigned).equals(parts[2])) {
        throw new IllegalArgumentException("Invalid JWT signature");
      }
      return objectMapper.readValue(URL_DECODER.decode(parts[1]), CLAIMS_TYPE);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid JWT", ex);
    }
  }

  private String hmac(String value) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(
        properties.security().jwt().secret().getBytes(StandardCharsets.UTF_8),
        "HmacSHA256"));
    return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
  }
}

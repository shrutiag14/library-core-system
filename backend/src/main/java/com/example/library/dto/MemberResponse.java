package com.example.library.dto;

import com.example.library.model.Member;
import com.example.library.model.MemberStatus;
import java.time.Instant;

public record MemberResponse(
    Long id,
    String name,
    String email,
    MemberStatus status,
    Instant createdAt,
    Instant updatedAt) {
  public static MemberResponse from(Member member) {
    return new MemberResponse(member.getId(), member.getName(), member.getEmail(),
        member.getStatus(), member.getCreatedAt(), member.getUpdatedAt());
  }
}

package com.example.library.service.impl;

import com.example.library.dto.MemberRequest;
import com.example.library.dto.MemberResponse;
import com.example.library.exception.ConflictException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Member;
import com.example.library.model.MemberStatus;
import com.example.library.repository.MemberRepository;
import com.example.library.service.AuditLogService;
import com.example.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final MemberRepository repository;
  private final AuditLogService auditLogService;

  @Override
  @Transactional(readOnly = true)
  public Page<MemberResponse> list(String search, Pageable pageable) {
    if (search == null || search.isBlank()) {
      return repository.findAll(pageable).map(MemberResponse::from);
    }
    return repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable)
        .map(MemberResponse::from);
  }

  @Override
  @Transactional(readOnly = true)
  public Member getEntity(Long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("Member not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public Member getActive(Long id) {
    return repository.findByIdAndStatus(id, MemberStatus.ACTIVE)
        .orElseThrow(() -> new NotFoundException("Active member not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public MemberResponse get(Long id) {
    return MemberResponse.from(getEntity(id));
  }

  @Override
  @Transactional
  public MemberResponse create(MemberRequest request) {
    if (repository.existsByEmail(request.email())) {
      throw new ConflictException("Member email already exists");
    }
    Member member = new Member();
    apply(member, request);
    Member saved = repository.save(member);
    auditLogService.record("CREATE_MEMBER", "Member", saved.getId(), saved.getEmail());
    return MemberResponse.from(saved);
  }

  @Override
  @Transactional
  public MemberResponse update(Long id, MemberRequest request) {
    if (repository.existsByEmailAndIdNot(request.email(), id)) {
      throw new ConflictException("Member email already exists");
    }
    Member member = getEntity(id);
    apply(member, request);
    auditLogService.record("UPDATE_MEMBER", "Member", member.getId(), member.getEmail());
    return MemberResponse.from(member);
  }

  @Override
  @Transactional
  public MemberResponse deactivate(Long id) {
    Member member = getEntity(id);
    member.setStatus(MemberStatus.INACTIVE);
    auditLogService.record("DEACTIVATE_MEMBER", "Member", member.getId(), member.getEmail());
    return MemberResponse.from(member);
  }

  private void apply(Member member, MemberRequest request) {
    member.setName(request.name().trim());
    member.setEmail(request.email().trim().toLowerCase());
    member.setStatus(request.status());
  }
}

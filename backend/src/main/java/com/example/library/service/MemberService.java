package com.example.library.service;

import com.example.library.dto.MemberRequest;
import com.example.library.dto.MemberResponse;
import com.example.library.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
  Page<MemberResponse> list(String search, Pageable pageable);

  Member getEntity(Long id);

  Member getActive(Long id);

  MemberResponse get(Long id);

  MemberResponse create(MemberRequest request);

  MemberResponse update(Long id, MemberRequest request);

  MemberResponse deactivate(Long id);
}

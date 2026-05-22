package com.example.library.controller;

import com.example.library.dto.MemberRequest;
import com.example.library.dto.MemberResponse;
import com.example.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@Validated
@RequiredArgsConstructor
public class MemberController {
  private final MemberService service;

  @GetMapping
  public Page<MemberResponse> list(@RequestParam(required = false) String search, Pageable pageable) {
    return service.list(search, pageable);
  }

  @GetMapping("/{id}")
  public MemberResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MemberResponse create(@Valid @RequestBody MemberRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/deactivate")
  public MemberResponse deactivate(@PathVariable Long id) {
    return service.deactivate(id);
  }
}

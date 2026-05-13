package com.example.u1proyecto_grupob.service;

import com.example.u1proyecto_grupob.dto.MemberRequestDto;
import com.example.u1proyecto_grupob.dto.MemberResponseDto;
import java.util.List;

public interface MemberService {
    MemberResponseDto create(MemberRequestDto dto);
    MemberResponseDto findById(Long id);
    List<MemberResponseDto> findAll();
    List<MemberResponseDto> findAllActive();
    void deactivate(Long id);
}

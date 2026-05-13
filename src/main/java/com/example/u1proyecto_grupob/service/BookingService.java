package com.example.u1proyecto_grupob.service;

import com.example.u1proyecto_grupob.domain.BookingStatus;
import com.example.u1proyecto_grupob.dto.BookingRequestDto;
import com.example.u1proyecto_grupob.dto.BookingResponseDto;
import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto dto);
    BookingResponseDto findById(Long id);
    List<BookingResponseDto> findAll();
    List<BookingResponseDto> findByMember(Long memberId);
    List<BookingResponseDto> findByWorkspace(Long workspaceId);
    List<BookingResponseDto> findByStatus(BookingStatus status);
    BookingResponseDto confirm(Long id);
    BookingResponseDto cancel(Long id);
}

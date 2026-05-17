package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto dto);
    BookingResponseDto findById(Long id);
    List<BookingResponseDto> findAll();
    Page<BookingResponseDto> searchPage(Long id, Pageable pageable);
    BookingResponseDto update(Long id, BookingRequestDto dto);
    void delete(Long id);
    List<BookingResponseDto> findByMember(Long memberId);
    List<BookingResponseDto> findByWorkspace(Long workspaceId);
    List<BookingResponseDto> findByStatus(BookingStatus status);
    BookingResponseDto confirm(Long id);
    BookingResponseDto cancel(Long id);
}
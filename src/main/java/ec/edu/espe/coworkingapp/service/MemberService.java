package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {
    MemberResponseDto create(MemberRequestDto dto);
    MemberResponseDto findById(Long id);
    List<MemberResponseDto> findAll();
    Page<MemberResponseDto> searchPage(String name, Pageable pageable);
    MemberResponseDto update(Long id, MemberRequestDto dto);
    void delete(Long id);
    List<MemberResponseDto> findAllActive();
    void deactivate(Long id);
    void activate(Long id);
}
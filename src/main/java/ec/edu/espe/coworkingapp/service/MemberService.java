package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import java.util.List;

public interface MemberService {
    MemberResponseDto create(MemberRequestDto dto);
    MemberResponseDto findById(Long id);
    List<MemberResponseDto> findAll();
    List<MemberResponseDto> findAllActive();
    void deactivate(Long id);
}
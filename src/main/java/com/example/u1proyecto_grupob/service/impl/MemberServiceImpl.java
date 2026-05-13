package com.example.u1proyecto_grupob.service.impl;

import com.example.u1proyecto_grupob.domain.Member;
import com.example.u1proyecto_grupob.domain.BookingStatus;
import com.example.u1proyecto_grupob.dto.MemberRequestDto;
import com.example.u1proyecto_grupob.dto.MemberResponseDto;
import com.example.u1proyecto_grupob.repository.MemberRepository;
import com.example.u1proyecto_grupob.repository.BookingRepository;
import com.example.u1proyecto_grupob.service.MemberService;
import com.example.u1proyecto_grupob.web.advice.BusinessConflictException;
import com.example.u1proyecto_grupob.web.advice.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;

    public MemberServiceImpl(MemberRepository memberRepository, BookingRepository bookingRepository) {
        this.memberRepository = memberRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public MemberResponseDto create(MemberRequestDto dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessConflictException("Ya existe un miembro con el email: " + dto.getEmail());
        }

        int maxHours;
        switch (dto.getPlanType()) {
            case BASICO: maxHours = 40; break;
            case ESTANDAR: maxHours = 80; break;
            case PREMIUM: maxHours = 160; break;
            default: maxHours = 0;
        }
        
        if (dto.getMonthlyHoursQuota() != null && dto.getMonthlyHoursQuota() > maxHours) {
            throw new BusinessConflictException("El cupo de horas supera el máximo permitido para su plan (" + maxHours + ")");
        }

        Member member = new Member();
        member.setFullName(dto.getFullName());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setPlanType(dto.getPlanType());
        member.setMonthlyHoursQuota(dto.getMonthlyHoursQuota() != null ? dto.getMonthlyHoursQuota() : maxHours);
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        Member savedMember = memberRepository.save(member);
        return toResponse(savedMember);
    }

    @Override
    public MemberResponseDto findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado con id: " + id));
        return toResponse(member);
    }

    @Override
    public List<MemberResponseDto> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponseDto> findAllActive() {
        return memberRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivate(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado con id: " + id));

        boolean hasActiveBookings = !bookingRepository.findByMemberIdAndStatusNot(id, BookingStatus.CANCELADA).isEmpty();
        if (hasActiveBookings) {
            throw new BusinessConflictException("El miembro tiene reservas activas, cancélalas antes de desactivarlo");
        }

        member.setActive(false);
        memberRepository.save(member);
    }

    private MemberResponseDto toResponse(Member m) {
        MemberResponseDto dto = new MemberResponseDto();
        dto.setId(m.getId());
        dto.setFullName(m.getFullName());
        dto.setEmail(m.getEmail());
        dto.setPhone(m.getPhone());
        dto.setPlanType(m.getPlanType());
        dto.setMonthlyHoursQuota(m.getMonthlyHoursQuota());
        dto.setActive(m.getActive());
        dto.setCreatedAt(m.getCreatedAt());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59);
        
        double used = bookingRepository.findByMemberIdAndStatusNotAndStartDatetimeBetween(m.getId(), BookingStatus.CANCELADA, startOfMonth, endOfMonth)
                .stream().mapToDouble(b -> b.getTotalHours()).sum();
        dto.setUsedHoursThisMonth(used);
        
        return dto;
    }
}

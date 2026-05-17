package ec.edu.espe.coworkingapp.service.impl;



import ec.edu.espe.coworkingapp.domain.BookingStatus;

import ec.edu.espe.coworkingapp.domain.Member;

import ec.edu.espe.coworkingapp.domain.PlanType;

import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;

import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;

import ec.edu.espe.coworkingapp.repository.BookingRepository;

import ec.edu.espe.coworkingapp.repository.MemberRepository;

import ec.edu.espe.coworkingapp.service.MemberService;

import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;

import ec.edu.espe.coworkingapp.web.advice.ResourceNotFoundException;

import org.springframework.stereotype.Service;



import java.time.LocalDateTime;

import java.time.YearMonth;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.PageImpl;



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



        int maxHours = getMaxHoursForPlan(dto.getPlanType());

        if (dto.getMonthlyHoursQuota() > maxHours) {

            throw new BusinessConflictException("El cupo mensual supera el lÃ­mite para el plan " + dto.getPlanType() + ". MÃ¡ximo: " + maxHours + "h.");

        }



        Member member = new Member();

        member.setFullName(dto.getFullName());

        member.setEmail(dto.getEmail());

        member.setPhone(dto.getPhone());

        member.setPlanType(dto.getPlanType());

        member.setMonthlyHoursQuota(dto.getMonthlyHoursQuota());

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

        return memberRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());

    }



    @Override

    public List<MemberResponseDto> findAllActive() {

        return memberRepository.findByActiveTrue().stream().map(this::toResponse).collect(Collectors.toList());

    }



    @Override
    public Page<MemberResponseDto> searchPage(String name, Pageable pageable) {
        Page<Member> members = (name != null && !name.trim().isEmpty()) 
            ? memberRepository.findByFullNameContainingIgnoreCase(name, pageable)
            : memberRepository.findAll(pageable);
        return members.map(this::toResponse);
    }
    @Override
    public MemberResponseDto update(Long id, MemberRequestDto dto) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (!member.getEmail().equals(dto.getEmail()) && memberRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessConflictException("Email already exists");
        }
        member.setFullName(dto.getFullName());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setPlanType(dto.getPlanType());
        return toResponse(memberRepository.save(member));
    }
    @Override
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }

    public void deactivate(Long id) {

        Member member = memberRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado con id: " + id));



        long activeBookingsCount = bookingRepository.findByMemberIdAndStatusNot(id, BookingStatus.CANCELADA).stream()

                .filter(b -> b.getStatus() == BookingStatus.PENDIENTE || b.getStatus() == BookingStatus.CONFIRMADA)

                .count();



        if (activeBookingsCount > 0) {

            throw new BusinessConflictException("El miembro tiene reservas activas, cancÃ©lalas antes de desactivarlo");

        }



        member.setActive(false);

        memberRepository.save(member);

    }



    private MemberResponseDto toResponse(Member m) {

        MemberResponseDto res = new MemberResponseDto();

        res.setId(m.getId());

        res.setFullName(m.getFullName());

        res.setEmail(m.getEmail());

        res.setPhone(m.getPhone());

        res.setPlanType(m.getPlanType());

        res.setMonthlyHoursQuota(m.getMonthlyHoursQuota());

        res.setActive(m.getActive());

        res.setCreatedAt(m.getCreatedAt());



        YearMonth currentMonth = YearMonth.now();

        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();

        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);



        double used = bookingRepository.findByMemberIdAndStatusNotAndStartDatetimeBetween(

                m.getId(), BookingStatus.CANCELADA, startOfMonth, endOfMonth)

                .stream().mapToDouble(b -> b.getTotalHours()).sum();



        res.setUsedHoursThisMonth(used);

        return res;

    }



    private int getMaxHoursForPlan(PlanType plan) {

        switch(plan) {

            case BASICO: return 40;

            case ESTANDAR: return 80;

            case PREMIUM: return 160;

            default: return 0;

        }

    }

}


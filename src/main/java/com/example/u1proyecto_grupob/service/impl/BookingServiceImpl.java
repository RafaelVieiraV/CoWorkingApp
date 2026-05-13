package com.example.u1proyecto_grupob.service.impl;

import com.example.u1proyecto_grupob.domain.*;
import com.example.u1proyecto_grupob.dto.*;
import com.example.u1proyecto_grupob.repository.*;
import com.example.u1proyecto_grupob.service.BookingService;
import com.example.u1proyecto_grupob.web.advice.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, MemberRepository memberRepository, WorkspaceRepository workspaceRepository) {
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public BookingResponseDto create(BookingRequestDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));
        if (!member.getActive()) {
            throw new BusinessConflictException("El miembro no está activo");
        }

        Workspace w = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado"));
        if (!w.getAvailable()) {
            throw new BusinessConflictException("El workspace no está disponible");
        }

        if (!dto.getEndDatetime().isAfter(dto.getStartDatetime())) {
            throw new BusinessConflictException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        if (dto.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new BusinessConflictException("No se puede crear una reserva en el pasado");
        }

        List<Booking> activeBookings = bookingRepository.findByWorkspaceIdAndStatusIn(w.getId(), List.of(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA));
        for (Booking b : activeBookings) {
            if (dto.getStartDatetime().isBefore(b.getEndDatetime()) && dto.getEndDatetime().isAfter(b.getStartDatetime())) {
                throw new BusinessConflictException("El workspace ya tiene una reserva en ese horario");
            }
        }

        double totalHours = Math.round((ChronoUnit.MINUTES.between(dto.getStartDatetime(), dto.getEndDatetime()) / 60.0) * 100.0) / 100.0;
        if (totalHours < 0.5) {
            throw new BusinessConflictException("La duración mínima de una reserva es 30 minutos");
        }

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59);
        double usedHours = bookingRepository.findByMemberIdAndStatusNotAndStartDatetimeBetween(member.getId(), BookingStatus.CANCELADA, startOfMonth, endOfMonth)
                .stream().mapToDouble(Booking::getTotalHours).sum();
        
        if (usedHours + totalHours > member.getMonthlyHoursQuota()) {
            throw new BusinessConflictException("El miembro no tiene suficiente cupo mensual disponible");
        }

        Booking booking = new Booking();
        booking.setMember(member);
        booking.setWorkspace(w);
        booking.setStartDatetime(dto.getStartDatetime());
        booking.setEndDatetime(dto.getEndDatetime());
        booking.setStatus(BookingStatus.PENDIENTE);
        booking.setTotalHours(totalHours);
        booking.setCreatedAt(LocalDateTime.now());

        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto findById(Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        return toResponse(b);
    }

    @Override
    public List<BookingResponseDto> findAll() {
        return bookingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> findByMember(Long memberId) {
        return bookingRepository.findByMemberId(memberId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> findByWorkspace(Long workspaceId) {
        return bookingRepository.findByWorkspaceId(workspaceId).stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponseDto> findByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public BookingResponseDto confirm(Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        if (b.getStatus() == BookingStatus.CONFIRMADA) throw new BusinessConflictException("La reserva ya está confirmada");
        if (b.getStatus() == BookingStatus.CANCELADA) throw new BusinessConflictException("No se puede confirmar una reserva cancelada");
        
        if (!b.getWorkspace().getAvailable()) throw new BusinessConflictException("El workspace ya no está disponible");
        
        b.setStatus(BookingStatus.CONFIRMADA);
        return toResponse(bookingRepository.save(b));
    }

    @Override
    public BookingResponseDto cancel(Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        if (b.getStatus() == BookingStatus.CANCELADA) throw new BusinessConflictException("La reserva ya está cancelada");
        
        b.setStatus(BookingStatus.CANCELADA);
        return toResponse(bookingRepository.save(b));
    }

    private BookingResponseDto toResponse(Booking b) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(b.getId());
        dto.setMemberId(b.getMember().getId());
        dto.setMemberFullName(b.getMember().getFullName());
        dto.setWorkspaceId(b.getWorkspace().getId());
        dto.setWorkspaceName(b.getWorkspace().getName());
        dto.setWorkspaceType(b.getWorkspace().getType());
        dto.setStartDatetime(b.getStartDatetime());
        dto.setEndDatetime(b.getEndDatetime());
        dto.setTotalHours(b.getTotalHours());
        
        double totalPrice = Math.round((b.getTotalHours() * b.getWorkspace().getPricePerHour()) * 100.0) / 100.0;
        dto.setTotalPrice(totalPrice);
        
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}

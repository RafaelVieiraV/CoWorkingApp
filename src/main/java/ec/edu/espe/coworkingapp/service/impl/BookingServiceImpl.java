package ec.edu.espe.coworkingapp.service.impl;



import ec.edu.espe.coworkingapp.domain.Booking;

import ec.edu.espe.coworkingapp.domain.BookingStatus;

import ec.edu.espe.coworkingapp.domain.Member;

import ec.edu.espe.coworkingapp.domain.Workspace;

import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;

import ec.edu.espe.coworkingapp.repository.BookingRepository;

import ec.edu.espe.coworkingapp.repository.MemberRepository;

import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;

import ec.edu.espe.coworkingapp.service.BookingService;

import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;

import ec.edu.espe.coworkingapp.web.advice.ResourceNotFoundException;

import org.springframework.stereotype.Service;



import java.time.LocalDateTime;

import java.time.YearMonth;

import java.time.temporal.ChronoUnit;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;



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

        Member m = memberRepository.findById(dto.getMemberId())

                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        if (!m.getActive()) throw new BusinessConflictException("El miembro no estÃ¡ activo");



        Workspace w = workspaceRepository.findById(dto.getWorkspaceId())

                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado"));

        if (!w.getAvailable()) throw new BusinessConflictException("El workspace no estÃ¡ disponible");



        if (!dto.getEndDatetime().isAfter(dto.getStartDatetime())) {

            throw new BusinessConflictException("La fecha de fin debe ser posterior a la fecha de inicio");

        }

        if (dto.getStartDatetime().isBefore(LocalDateTime.now())) {

            throw new BusinessConflictException("No se puede crear una reserva en el pasado");

        }



        double totalHours = ChronoUnit.MINUTES.between(dto.getStartDatetime(), dto.getEndDatetime()) / 60.0;

        totalHours = Math.round(totalHours * 100.0) / 100.0;



        if (totalHours < 0.5) {

            throw new BusinessConflictException("La duraciÃ³n mÃ­nima de una reserva es 30 minutos");

        }



        boolean overlap = bookingRepository.findByWorkspaceIdAndStatusIn(w.getId(), List.of(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA))

                .stream().anyMatch(b -> b.getStartDatetime().isBefore(dto.getEndDatetime()) && b.getEndDatetime().isAfter(dto.getStartDatetime()));



        if (overlap) {

            throw new BusinessConflictException("El workspace ya tiene una reserva en ese horario");

        }



        YearMonth cm = YearMonth.now();

        double used = bookingRepository.findByMemberIdAndStatusNotAndStartDatetimeBetween(

                m.getId(), BookingStatus.CANCELADA, cm.atDay(1).atStartOfDay(), cm.atEndOfMonth().atTime(23,59,59)

        ).stream().mapToDouble(b -> b.getTotalHours()).sum();



        if (used + totalHours > m.getMonthlyHoursQuota()) {

            throw new BusinessConflictException("El miembro no tiene suficiente cupo mensual disponible");

        }



        Booking b = new Booking();

        b.setMember(m);

        b.setWorkspace(w);

        b.setStartDatetime(dto.getStartDatetime());

        b.setEndDatetime(dto.getEndDatetime());

        b.setStatus(BookingStatus.PENDIENTE);

        b.setTotalHours(totalHours);

        b.setCreatedAt(LocalDateTime.now());



        return toResponse(bookingRepository.save(b));

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
    public Page<BookingResponseDto> searchPage(Long id, Pageable pageable) {
        Page<Booking> bookings = (id != null) 
            ? bookingRepository.findById(id, pageable)
            : bookingRepository.findAll(pageable);
        return bookings.map(this::toResponse);
    }
    @Override
    public BookingResponseDto update(Long id, BookingRequestDto dto) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        // Basic update simulation
        booking.setStartDatetime(dto.getStartDatetime());
        booking.setEndDatetime(dto.getEndDatetime());
        return toResponse(bookingRepository.save(booking));
    }
    @Override
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    public BookingResponseDto confirm(Long id) {

        Booking b = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (b.getStatus() == BookingStatus.CONFIRMADA) throw new BusinessConflictException("La reserva ya estÃ¡ confirmada");

        if (b.getStatus() == BookingStatus.CANCELADA) throw new BusinessConflictException("No se puede confirmar una reserva cancelada");

        

        if (!b.getWorkspace().getAvailable()) throw new BusinessConflictException("El workspace ya no estÃ¡ disponible");



        b.setStatus(BookingStatus.CONFIRMADA);

        return toResponse(bookingRepository.save(b));

    }



    @Override

    public BookingResponseDto cancel(Long id) {

        Booking b = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (b.getStatus() == BookingStatus.CANCELADA) throw new BusinessConflictException("La reserva ya estÃ¡ cancelada");



        b.setStatus(BookingStatus.CANCELADA);

        return toResponse(bookingRepository.save(b));

    }



    private BookingResponseDto toResponse(Booking b) {

        BookingResponseDto res = new BookingResponseDto();

        res.setId(b.getId());

        res.setMemberId(b.getMember().getId());

        res.setMemberFullName(b.getMember().getFullName());

        res.setWorkspaceId(b.getWorkspace().getId());

        res.setWorkspaceName(b.getWorkspace().getName());

        res.setWorkspaceType(b.getWorkspace().getType());

        res.setStartDatetime(b.getStartDatetime());

        res.setEndDatetime(b.getEndDatetime());

        res.setTotalHours(b.getTotalHours());

        

        double price = b.getTotalHours() * b.getWorkspace().getPricePerHour();

        res.setTotalPrice(Math.round(price * 100.0) / 100.0);

        

        res.setStatus(b.getStatus());

        res.setCreatedAt(b.getCreatedAt());

        return res;

    }

}


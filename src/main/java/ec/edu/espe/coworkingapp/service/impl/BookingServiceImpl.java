package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.domain.Member;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.dto.request.BookingRequest;
import ec.edu.espe.coworkingapp.dto.response.BookingResponse;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import ec.edu.espe.coworkingapp.service.BookingService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              MemberRepository memberRepository,
                              WorkspaceRepository workspaceRepository) {
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<BookingResponse> findAll() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con id: " + id));
        return BookingResponse.from(booking);
    }

    @Override
    public BookingResponse create(BookingRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado con id: " + request.getMemberId()));

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace no encontrado con id: " + request.getWorkspaceId()));

        if (!workspace.getAvailable()) {
            throw new RuntimeException("El workspace no está disponible");
        }

        long minutes = Duration.between(request.getStartDatetime(), request.getEndDatetime()).toMinutes();
        if (minutes <= 0) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la de inicio");
        }

        Booking booking = new Booking();
        booking.setMember(member);
        booking.setWorkspace(workspace);
        booking.setStartDatetime(request.getStartDatetime());
        booking.setEndDatetime(request.getEndDatetime());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setTotalHours(BigDecimal.valueOf(minutes / 60.0));

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse cancel(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con id: " + id));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponse> findByMember(Long memberId) {
        return bookingRepository.findByMemberId(memberId)
                .stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
}
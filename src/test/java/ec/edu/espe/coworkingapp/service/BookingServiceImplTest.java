package ec.edu.espe.coworkingapp.service;
import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Member;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import ec.edu.espe.coworkingapp.service.impl.BookingServiceImpl;
import ec.edu.espe.coworkingapp.reactive.service.BookingEventStreamService;
import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;
import ec.edu.espe.coworkingapp.web.advice.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BookingServiceImplTest {

    private BookingServiceImpl bookingService;
    private BookingRepository bookingRepository;
    private MemberRepository memberRepository;
    private WorkspaceRepository workspaceRepository;
    private MemberBlockClient memberBlockClient;
    private BookingEventStreamService bookingEventStreamService;

    @BeforeEach
    public void setUp() {
        // Se crean los mocks a mano y se inyectan por constructor
        bookingRepository = Mockito.mock(BookingRepository.class);
        memberRepository = Mockito.mock(MemberRepository.class);
        workspaceRepository = Mockito.mock(WorkspaceRepository.class);
        memberBlockClient = Mockito.mock(MemberBlockClient.class);
        bookingEventStreamService = Mockito.mock(BookingEventStreamService.class);
        bookingService = new BookingServiceImpl(
                bookingRepository, memberRepository, workspaceRepository, memberBlockClient, bookingEventStreamService);
    }

    // ═══════════════════════════ create ═══════════════════════════

    // FUNCIÓN: crear reserva válida -> guarda y retorna respuesta (ArgumentCaptor + times)
    @Test
    void create_validData_shouldSaveAndReturnResponse() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        BookingRequestDto dto = buildRequest(start, end);

        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(buildMember()));
        Mockito.when(memberBlockClient.isBlocked("jjguerra5@espe.edu.ec")).thenReturn(false);
        Mockito.when(workspaceRepository.findById(10L)).thenReturn(Optional.of(buildWorkspace()));
        Mockito.when(bookingRepository.save(ArgumentMatchers.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        BookingResponseDto response = bookingService.create(dto);

        // Assert
        Assertions.assertNotNull(response);
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository, Mockito.times(1)).save(captor.capture());
        Booking saved = captor.getValue();
        Assertions.assertEquals(BookingStatus.PENDIENTE, saved.getStatus());
        Assertions.assertEquals(2.0, saved.getTotalHours());
        Assertions.assertEquals(1L, saved.getMember().getId());
    }

    // VALIDACIÓN: miembro no encontrado -> excepción y no toca otras dependencias
    @Test
    void create_memberNotFound_shouldThrow_andNotCallOthers() {
        // Arrange
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookingService.create(dto));
        Mockito.verifyNoInteractions(memberBlockClient, workspaceRepository, bookingRepository);
    }

    // VALIDACIÓN: miembro inactivo -> excepción y no llama al bloqueo/workspace/repo
    @Test
    void create_memberNotActive_shouldThrow() {
        // Arrange
        Member inactivo = buildMember();
        inactivo.setActive(false);
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(inactivo));

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verifyNoInteractions(memberBlockClient, workspaceRepository, bookingRepository);
    }

    // VALIDACIÓN: miembro bloqueado -> excepción, se consulta isBlocked y NO se guarda
    @Test
    void create_memberBlocked_shouldThrow_andNotSave() {
        // Arrange
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(buildMember()));
        Mockito.when(memberBlockClient.isBlocked("jjguerra5@espe.edu.ec")).thenReturn(true);

        // Act + Assert
        BusinessConflictException ex = Assertions.assertThrows(BusinessConflictException.class,
                () -> bookingService.create(dto));
        Assertions.assertEquals("El miembro está bloqueado y no puede realizar reservas", ex.getMessage());
        Mockito.verify(memberBlockClient, Mockito.times(1)).isBlocked("jjguerra5@espe.edu.ec");
        Mockito.verifyNoInteractions(workspaceRepository, bookingRepository);
    }

    // VALIDACIÓN: workspace no encontrado -> excepción y no guarda
    @Test
    void create_workspaceNotFound_shouldThrow() {
        // Arrange
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(buildMember()));
        Mockito.when(memberBlockClient.isBlocked("jjguerra5@espe.edu.ec")).thenReturn(false);
        Mockito.when(workspaceRepository.findById(10L)).thenReturn(Optional.empty());

        // Act + Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: workspace no disponible -> excepción y no guarda
    @Test
    void create_workspaceNotAvailable_shouldThrow() {
        // Arrange
        Workspace noDisponible = buildWorkspace();
        noDisponible.setAvailable(false);
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(buildMember()));
        Mockito.when(memberBlockClient.isBlocked("jjguerra5@espe.edu.ec")).thenReturn(false);
        Mockito.when(workspaceRepository.findById(10L)).thenReturn(Optional.of(noDisponible));

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: fecha fin no posterior al inicio -> excepción y no guarda
    @Test
    void create_endNotAfterStart_shouldThrow() {
        // Arrange (fin ANTES del inicio)
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusHours(1);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: reserva en el pasado -> excepción y no guarda
    @Test
    void create_startInPast_shouldThrow() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = start.plusHours(2);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: duración menor a 30 minutos -> excepción y no guarda
    @Test
    void create_durationLessThan30Min_shouldThrow() {
        // Arrange (solo 20 minutos)
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusMinutes(20);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: solapamiento de horario -> excepción y no guarda
    @Test
    void create_overlap_shouldThrow() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();

        Booking existente = new Booking();
        existente.setStartDatetime(start);
        existente.setEndDatetime(end);
        Mockito.when(bookingRepository.findByWorkspaceIdAndStatusIn(
                        ArgumentMatchers.eq(10L), ArgumentMatchers.anyList()))
                .thenReturn(List.of(existente));

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: cupo mensual insuficiente -> excepción y no guarda
    @Test
    void create_quotaExceeded_shouldThrow() {
        // Arrange (ya usó 39h de 40, +2h nuevas = 41 > 40)
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();

        Booking usadas = new Booking();
        usadas.setTotalHours(39.0);
        Mockito.when(bookingRepository.findByMemberIdAndStatusNotAndStartDatetimeBetween(
                        ArgumentMatchers.eq(1L), ArgumentMatchers.eq(BookingStatus.CANCELADA),
                        ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(List.of(usadas));

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.create(dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // ORDEN DE EJECUCIÓN (InOrder): buscar miembro -> consultar bloqueo -> buscar workspace -> guardar
    @Test
    void create_executionOrder_inOrder() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        BookingRequestDto dto = buildRequest(start, end);
        stubMemberAndWorkspaceOk();
        Mockito.when(bookingRepository.save(ArgumentMatchers.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        bookingService.create(dto);

        // Assert
        InOrder inOrder = Mockito.inOrder(memberRepository, memberBlockClient, workspaceRepository, bookingRepository);
        inOrder.verify(memberRepository).findById(1L);
        inOrder.verify(memberBlockClient).isBlocked("jjguerra5@espe.edu.ec");
        inOrder.verify(workspaceRepository).findById(10L);
        inOrder.verify(bookingRepository).save(ArgumentMatchers.any(Booking.class));
    }

    // ═══════════════════════════ findById ═══════════════════════════

    // FUNCIÓN: buscar por id existente -> retorna respuesta
    @Test
    void findById_existing_shouldReturnResponse() {
        // Arrange
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.PENDIENTE)));

        // Act
        BookingResponseDto response = bookingService.findById(100L);

        // Assert
        Assertions.assertNotNull(response);
        Mockito.verify(bookingRepository, Mockito.times(1)).findById(100L);
    }

    // VALIDACIÓN: buscar por id inexistente -> excepción
    @Test
    void findById_notFound_shouldThrow() {
        // Arrange
        Mockito.when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookingService.findById(99L));
    }

    // ═══════════════════════════ listados ═══════════════════════════

    // FUNCIÓN: findAll -> retorna la lista mapeada
    @Test
    void findAll_shouldReturnList() {
        Mockito.when(bookingRepository.findAll()).thenReturn(List.of(buildBooking(BookingStatus.PENDIENTE)));
        List<BookingResponseDto> result = bookingService.findAll();
        Assertions.assertEquals(1, result.size());
    }

    // FUNCIÓN: findByMember -> retorna las reservas del miembro
    @Test
    void findByMember_shouldReturnList() {
        Mockito.when(bookingRepository.findByMemberId(1L)).thenReturn(List.of(buildBooking(BookingStatus.PENDIENTE)));
        List<BookingResponseDto> result = bookingService.findByMember(1L);
        Assertions.assertEquals(1, result.size());
    }

    // FUNCIÓN: findByWorkspace -> retorna las reservas del workspace
    @Test
    void findByWorkspace_shouldReturnList() {
        Mockito.when(bookingRepository.findByWorkspaceId(10L)).thenReturn(List.of(buildBooking(BookingStatus.PENDIENTE)));
        List<BookingResponseDto> result = bookingService.findByWorkspace(10L);
        Assertions.assertEquals(1, result.size());
    }

    // FUNCIÓN: findByStatus -> retorna las reservas por estado
    @Test
    void findByStatus_shouldReturnList() {
        Mockito.when(bookingRepository.findByStatus(BookingStatus.PENDIENTE))
                .thenReturn(List.of(buildBooking(BookingStatus.PENDIENTE)));
        List<BookingResponseDto> result = bookingService.findByStatus(BookingStatus.PENDIENTE);
        Assertions.assertEquals(1, result.size());
    }

    // ═══════════════════════════ searchPage ═══════════════════════════

    // FUNCIÓN: buscar con id -> usa findById paginado
    @Test
    void searchPage_withId_shouldUseFindById() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> page = new PageImpl<>(List.of(buildBooking(BookingStatus.PENDIENTE)));
        Mockito.when(bookingRepository.findById(100L, pageable)).thenReturn(page);

        Page<BookingResponseDto> result = bookingService.searchPage(100L, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(bookingRepository).findById(100L, pageable);
        Mockito.verify(bookingRepository, Mockito.never()).findAll(pageable);
    }

    // FUNCIÓN: buscar sin id -> usa findAll paginado
    @Test
    void searchPage_withoutId_shouldUseFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> page = new PageImpl<>(List.of(buildBooking(BookingStatus.PENDIENTE)));
        Mockito.when(bookingRepository.findAll(pageable)).thenReturn(page);

        Page<BookingResponseDto> result = bookingService.searchPage(null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(bookingRepository).findAll(pageable);
    }

    // ═══════════════════════════ update ═══════════════════════════

    // FUNCIÓN: actualizar reserva existente -> guarda y retorna (ArgumentCaptor)
    @Test
    void update_existing_shouldSaveAndReturn() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusHours(3);
        BookingRequestDto dto = buildRequest(start, end);

        Mockito.when(bookingRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(buildBooking(BookingStatus.PENDIENTE)));
        Mockito.when(memberRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(buildMember()));
        Mockito.when(workspaceRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(buildWorkspace()));
        Mockito.when(bookingRepository.save(ArgumentMatchers.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        bookingService.update(100L, dto);

        // Assert
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository).save(captor.capture());
        Assertions.assertEquals(start, captor.getValue().getStartDatetime());
        Assertions.assertEquals(end, captor.getValue().getEndDatetime());
    }

    // VALIDACIÓN: actualizar reserva inexistente -> excepción y no guarda
    @Test
    void update_notFound_shouldThrow() {
        BookingRequestDto dto = buildRequest(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Mockito.when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookingService.update(99L, dto));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // ═══════════════════════════ delete ═══════════════════════════

    // FUNCIÓN: eliminar -> delega en deleteById (times + verifyNoMoreInteractions + verifyNoInteractions)
    @Test
    void delete_shouldCallDeleteById() {
        // Act
        bookingService.delete(100L);

        // Assert
        Mockito.verify(bookingRepository, Mockito.times(1)).deleteById(100L);
        Mockito.verifyNoMoreInteractions(bookingRepository);
        Mockito.verifyNoInteractions(memberRepository, workspaceRepository, memberBlockClient);
    }

    // ═══════════════════════════ confirm ═══════════════════════════

    // FUNCIÓN: confirmar reserva pendiente -> pasa a CONFIRMADA y guarda (ArgumentCaptor)
    @Test
    void confirm_pending_shouldConfirmAndSave() {
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.PENDIENTE)));
        Mockito.when(bookingRepository.save(ArgumentMatchers.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        bookingService.confirm(100L);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository).save(captor.capture());
        Assertions.assertEquals(BookingStatus.CONFIRMADA, captor.getValue().getStatus());
    }

    // VALIDACIÓN: confirmar una ya confirmada -> excepción y no guarda
    @Test
    void confirm_alreadyConfirmed_shouldThrow() {
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.CONFIRMADA)));

        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.confirm(100L));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: confirmar una cancelada -> excepción y no guarda
    @Test
    void confirm_cancelled_shouldThrow() {
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.CANCELADA)));

        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.confirm(100L));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // VALIDACIÓN: confirmar cuando el workspace ya no está disponible -> excepción y no guarda
    @Test
    void confirm_workspaceNotAvailable_shouldThrow() {
        Booking b = buildBooking(BookingStatus.PENDIENTE);
        b.getWorkspace().setAvailable(false);
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.confirm(100L));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // ═══════════════════════════ cancel ═══════════════════════════

    // FUNCIÓN: cancelar reserva pendiente -> pasa a CANCELADA y guarda
    @Test
    void cancel_pending_shouldCancelAndSave() {
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.PENDIENTE)));
        Mockito.when(bookingRepository.save(ArgumentMatchers.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        bookingService.cancel(100L);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        Mockito.verify(bookingRepository).save(captor.capture());
        Assertions.assertEquals(BookingStatus.CANCELADA, captor.getValue().getStatus());
    }

    // VALIDACIÓN: cancelar una ya cancelada -> excepción y no guarda
    @Test
    void cancel_alreadyCancelled_shouldThrow() {
        Mockito.when(bookingRepository.findById(100L)).thenReturn(Optional.of(buildBooking(BookingStatus.CANCELADA)));

        Assertions.assertThrows(BusinessConflictException.class, () -> bookingService.cancel(100L));
        Mockito.verify(bookingRepository, Mockito.never()).save(ArgumentMatchers.any(Booking.class));
    }

    // ═══════════════════════════ Helpers ═══════════════════════════

    private Member buildMember() {
        Member m = new Member();
        m.setId(1L);
        m.setFullName("Juan Guerra");
        m.setEmail("jjguerra5@espe.edu.ec");
        m.setActive(true);
        m.setBlocked(false);
        m.setMonthlyHoursQuota(40);
        return m;
    }

    private Workspace buildWorkspace() {
        Workspace w = new Workspace();
        w.setId(10L);
        w.setName("Escritorio 1");
        w.setType(WorkspaceType.ESCRITORIO);
        w.setPricePerHour(5.0);
        w.setAvailable(true);
        return w;
    }

    private BookingRequestDto buildRequest(LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setMemberId(1L);
        dto.setWorkspaceId(10L);
        dto.setStartDatetime(start);
        dto.setEndDatetime(end);
        return dto;
    }

    private Booking buildBooking(BookingStatus status) {
        Booking b = new Booking();
        b.setId(100L);
        b.setMember(buildMember());
        b.setWorkspace(buildWorkspace());
        b.setStartDatetime(LocalDateTime.now().plusDays(1));
        b.setEndDatetime(LocalDateTime.now().plusDays(1).plusHours(2));
        b.setTotalHours(2.0);
        b.setStatus(status);
        b.setCreatedAt(LocalDateTime.now());
        return b;
    }

    // Deja miembro (activo, no bloqueado) y workspace (disponible) listos para pasar
    // las primeras validaciones y llegar a las de fecha/solapamiento/cupo.
    private void stubMemberAndWorkspaceOk() {
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(buildMember()));
        Mockito.when(memberBlockClient.isBlocked("jjguerra5@espe.edu.ec")).thenReturn(false);
        Mockito.when(workspaceRepository.findById(10L)).thenReturn(Optional.of(buildWorkspace()));
    }
}

package ec.edu.espe.coworkingapp.service;


import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Member;
import ec.edu.espe.coworkingapp.domain.PlanType;
import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.service.impl.MemberServiceImpl;
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

import java.util.List;
import java.util.Optional;

public class MemberServiceImplTest {

    private MemberServiceImpl memberService;
    private MemberRepository memberRepository;
    private BookingRepository bookingRepository;

    // Dato base reutilizado por cada prueba
    private Member member;

    @BeforeEach
    public void setUp() {
        // Mocks a mano e inyección por constructor
        memberRepository = Mockito.mock(MemberRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        memberService = new MemberServiceImpl(memberRepository, bookingRepository);

        member = new Member();
        member.setId(1L);
        member.setFullName("Juan Guerra");
        member.setEmail("jjguerra5@espe.edu.ec");
        member.setPhone("0999999999");
        member.setPlanType(PlanType.BASICO);
        member.setMonthlyHoursQuota(40);
        member.setActive(true);
        member.setBlocked(false);
    }

    // ═══════════════════════════ create ═══════════════════════════

    // FUNCIÓN: crear miembro válido -> guarda y retorna (ArgumentCaptor + times)
    @Test
    void create_validData_shouldSaveAndReturnResponse() {
        // Arrange
        MemberRequestDto dto = buildRequest("Juan Guerra", "jjguerra5@espe.edu.ec", PlanType.BASICO, 40);
        Mockito.when(memberRepository.existsByEmail("jjguerra5@espe.edu.ec")).thenReturn(false);
        Mockito.when(memberRepository.save(ArgumentMatchers.any(Member.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        MemberResponseDto response = memberService.create(dto);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("jjguerra5@espe.edu.ec", response.getEmail());
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository, Mockito.times(1)).save(captor.capture());
        Assertions.assertTrue(captor.getValue().getActive()); // nace activo
    }

    // VALIDACIÓN: email ya existente -> excepción, no guarda y no toca reservas
    @Test
    void create_emailAlreadyExists_shouldThrow_andNotSave() {
        // Arrange
        MemberRequestDto dto = buildRequest("Juan Guerra", "jjguerra5@espe.edu.ec", PlanType.BASICO, 40);
        Mockito.when(memberRepository.existsByEmail("jjguerra5@espe.edu.ec")).thenReturn(true);

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> memberService.create(dto));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // VALIDACIÓN: cupo supera el límite del plan -> excepción y no guarda
    @Test
    void create_quotaExceedsPlanLimit_shouldThrow_andNotSave() {
        // Arrange (BASICO permite máximo 40h, pedimos 50h)
        MemberRequestDto dto = buildRequest("Juan Guerra", "jjguerra5@espe.edu.ec", PlanType.BASICO, 50);
        Mockito.when(memberRepository.existsByEmail("jjguerra5@espe.edu.ec")).thenReturn(false);

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> memberService.create(dto));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // ═══════════════════════════ findById ═══════════════════════════

    // FUNCIÓN: buscar por id existente -> retorna respuesta
    @Test
    void findById_existing_shouldReturnResponse() {
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberResponseDto response = memberService.findById(1L);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Mockito.verify(memberRepository, Mockito.times(1)).findById(1L);
    }

    // VALIDACIÓN: buscar por id inexistente -> excepción
    @Test
    void findById_notFound_shouldThrow() {
        Mockito.when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> memberService.findById(99L));
    }

    // ═══════════════════════════ listados ═══════════════════════════

    // FUNCIÓN: findAll -> lista mapeada
    @Test
    void findAll_shouldReturnList() {
        Mockito.when(memberRepository.findAll()).thenReturn(List.of(member));
        Assertions.assertEquals(1, memberService.findAll().size());
    }

    // FUNCIÓN: findAllActive -> solo activos
    @Test
    void findAllActive_shouldReturnList() {
        Mockito.when(memberRepository.findByActiveTrue()).thenReturn(List.of(member));
        Assertions.assertEquals(1, memberService.findAllActive().size());
    }

    // ═══════════════════════════ searchPage ═══════════════════════════

    // FUNCIÓN: buscar con nombre -> consulta por nombre
    @Test
    void searchPage_withName_shouldUseNameQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(member));
        Mockito.when(memberRepository.findByFullNameContainingIgnoreCase("Juan", pageable)).thenReturn(page);

        // Sin filtro de estado (active = null) -> usa la búsqueda por nombre
        Page<MemberResponseDto> result = memberService.searchPage("Juan", null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(memberRepository).findByFullNameContainingIgnoreCase("Juan", pageable);
        Mockito.verify(memberRepository, Mockito.never()).findAll(pageable);
    }

    // FUNCIÓN: buscar sin nombre -> findAll paginado
    @Test
    void searchPage_withoutName_shouldUseFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(member));
        Mockito.when(memberRepository.findAll(pageable)).thenReturn(page);

        Page<MemberResponseDto> result = memberService.searchPage(null, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(memberRepository).findAll(pageable);
    }

    // ═══════════════════════════ update ═══════════════════════════

    // FUNCIÓN: actualizar con el mismo email -> guarda y retorna (ArgumentCaptor)
    @Test
    void update_sameEmail_shouldSaveAndReturn() {
        // Arrange
        MemberRequestDto dto = buildRequest("Juan Editado", "jjguerra5@espe.edu.ec", PlanType.ESTANDAR, 60);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(memberRepository.save(ArgumentMatchers.any(Member.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        memberService.update(1L, dto);

        // Assert
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository).save(captor.capture());
        Assertions.assertEquals("Juan Editado", captor.getValue().getFullName());
        // Email sin cambios -> nunca valida duplicado
        Mockito.verify(memberRepository, Mockito.never()).existsByEmail(ArgumentMatchers.anyString());
    }

    // VALIDACIÓN: actualizar miembro inexistente -> excepción y no guarda
    @Test
    void update_memberNotFound_shouldThrow() {
        MemberRequestDto dto = buildRequest("Juan Guerra", "jjguerra5@espe.edu.ec", PlanType.BASICO, 40);
        Mockito.when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> memberService.update(99L, dto));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // VALIDACIÓN: cambiar a un email ya usado -> excepción y no guarda
    @Test
    void update_newEmailAlreadyExists_shouldThrow_andNotSave() {
        MemberRequestDto dto = buildRequest("Juan Guerra", "otro@espe.edu.ec", PlanType.BASICO, 40);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(memberRepository.existsByEmail("otro@espe.edu.ec")).thenReturn(true);

        Assertions.assertThrows(BusinessConflictException.class, () -> memberService.update(1L, dto));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // ═══════════════════════════ delete ═══════════════════════════

    // FUNCIÓN: eliminar -> delega en deleteById (times + verifyNoMoreInteractions + verifyNoInteractions)
    @Test
    void delete_shouldCallDeleteById() {
        memberService.delete(1L);

        Mockito.verify(memberRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verifyNoMoreInteractions(memberRepository);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // ═══════════════════════════ deactivate ═══════════════════════════

    // FUNCIÓN: desactivar sin reservas activas -> guarda con active=false (ArgumentCaptor)
    @Test
    void deactivate_noActiveBookings_shouldDeactivateAndSave() {
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(bookingRepository.findByMemberIdAndStatusNot(1L, BookingStatus.CANCELADA))
                .thenReturn(List.of());

        memberService.deactivate(1L);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository).save(captor.capture());
        Assertions.assertFalse(captor.getValue().getActive());
    }

    // VALIDACIÓN: desactivar con reservas activas -> excepción y no guarda
    @Test
    void deactivate_withActiveBookings_shouldThrow_andNotSave() {
        Booking activa = new Booking();
        activa.setStatus(BookingStatus.PENDIENTE);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(bookingRepository.findByMemberIdAndStatusNot(1L, BookingStatus.CANCELADA))
                .thenReturn(List.of(activa));

        Assertions.assertThrows(BusinessConflictException.class, () -> memberService.deactivate(1L));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // VALIDACIÓN: desactivar miembro inexistente -> excepción
    @Test
    void deactivate_memberNotFound_shouldThrow() {
        Mockito.when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> memberService.deactivate(99L));
    }

    // ═══════════════════════════ block ═══════════════════════════

    // FUNCIÓN: bloquear un miembro no bloqueado -> guarda con blocked=true (ArgumentCaptor)
    @Test
    void block_notBlocked_shouldBlockAndSave() {
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.block(1L);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository).save(captor.capture());
        Assertions.assertTrue(captor.getValue().getBlocked());
    }

    // VALIDACIÓN: bloquear un miembro ya bloqueado -> excepción y no guarda
    @Test
    void block_alreadyBlocked_shouldThrow_andNotSave() {
        member.setBlocked(true);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        BusinessConflictException ex = Assertions.assertThrows(BusinessConflictException.class,
                () -> memberService.block(1L));
        Assertions.assertEquals("El miembro ya se encuentra bloqueado", ex.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // VALIDACIÓN: bloquear un miembro inexistente -> excepción y no guarda
    @Test
    void block_memberNotFound_shouldThrow() {
        Mockito.when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> memberService.block(99L));
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // ═══════════════════════════ unblock ═══════════════════════════

    // FUNCIÓN: desbloquear un miembro bloqueado -> guarda con blocked=false (ArgumentCaptor)
    @Test
    void unblock_blocked_shouldUnblockAndSave() {
        member.setBlocked(true);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.unblock(1L);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository).save(captor.capture());
        Assertions.assertFalse(captor.getValue().getBlocked());
    }

    // VALIDACIÓN: desbloquear un miembro NO bloqueado -> excepción y no guarda
    @Test
    void unblock_notBlocked_shouldThrow_andNotSave() {
        member.setBlocked(false);
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        BusinessConflictException ex = Assertions.assertThrows(BusinessConflictException.class,
                () -> memberService.unblock(1L));
        Assertions.assertEquals("El miembro no se encuentra bloqueado", ex.getMessage());
        Mockito.verify(memberRepository, Mockito.never()).save(ArgumentMatchers.any(Member.class));
    }

    // ═══════════════════════ InOrder (orden de ejecución) ═══════════════════════

    // block: primero busca (findById) y luego guarda (save)
    @Test
    void block_shouldFindBeforeSave_inOrder() {
        Mockito.when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.block(1L);

        InOrder inOrder = Mockito.inOrder(memberRepository);
        inOrder.verify(memberRepository).findById(1L);
        inOrder.verify(memberRepository).save(ArgumentMatchers.any(Member.class));
    }

    // ═══════════════════════════ Helper ═══════════════════════════

    private MemberRequestDto buildRequest(String fullName, String email, PlanType plan, Integer quota) {
        MemberRequestDto dto = new MemberRequestDto();
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setPhone("0999999999");
        dto.setPlanType(plan);
        dto.setMonthlyHoursQuota(quota);
        return dto;
    }
}
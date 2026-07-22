package ec.edu.espe.coworkingapp.service;
import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import ec.edu.espe.coworkingapp.service.impl.WorkspaceServiceImpl;
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

public class WorkspaceServiceImplTest {

    private WorkspaceServiceImpl workspaceService;
    private WorkspaceRepository workspaceRepository;
    private BookingRepository bookingRepository;

    @BeforeEach
    public void setUp() {
        // Mocks a mano e inyección por constructor
        workspaceRepository = Mockito.mock(WorkspaceRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        workspaceService = new WorkspaceServiceImpl(workspaceRepository, bookingRepository);
    }

    // ═══════════════════════════ create ═══════════════════════════

    // FUNCIÓN: crear workspace válido -> guarda y retorna (ArgumentCaptor + times)
    @Test
    void create_validData_shouldSaveAndReturnResponse() {
        // Arrange (ESCRITORIO permite hasta 4 personas)
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.ESCRITORIO, 4, 1);
        Mockito.when(workspaceRepository.existsByName("Escritorio 1")).thenReturn(false);
        Mockito.when(workspaceRepository.save(ArgumentMatchers.any(Workspace.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        WorkspaceResponseDto response = workspaceService.create(dto);

        // Assert
        Assertions.assertNotNull(response);
        ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
        Mockito.verify(workspaceRepository, Mockito.times(1)).save(captor.capture());
        Assertions.assertTrue(captor.getValue().getAvailable()); // nace disponible
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // VALIDACIÓN: nombre ya existente -> excepción y no guarda
    @Test
    void create_nameAlreadyExists_shouldThrow_andNotSave() {
        // Arrange
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.ESCRITORIO, 4, 1);
        Mockito.when(workspaceRepository.existsByName("Escritorio 1")).thenReturn(true);

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.create(dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // VALIDACIÓN: capacidad no válida para el tipo -> excepción y no guarda
    @Test
    void create_invalidCapacityForType_shouldThrow() {
        // Arrange (CABINA permite máximo 2, pedimos 5)
        WorkspaceRequestDto dto = buildRequest("Cabina 1", WorkspaceType.CABINA, 5, 1);
        Mockito.when(workspaceRepository.existsByName("Cabina 1")).thenReturn(false);

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.create(dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: piso menor o igual a 0 -> excepción y no guarda
    @Test
    void create_invalidFloor_shouldThrow() {
        // Arrange
        WorkspaceRequestDto dto = buildRequest("Escritorio 2", WorkspaceType.ESCRITORIO, 4, 0);
        Mockito.when(workspaceRepository.existsByName("Escritorio 2")).thenReturn(false);

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.create(dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // ═══════════════════════════ findById ═══════════════════════════

    // FUNCIÓN: buscar por id existente -> retorna respuesta
    @Test
    void findById_existing_shouldReturnResponse() {
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));

        WorkspaceResponseDto response = workspaceService.findById(10L);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(10L, response.getId());
        Mockito.verify(workspaceRepository, Mockito.times(1)).findById(10L);
    }

    // VALIDACIÓN: buscar por id inexistente -> excepción
    @Test
    void findById_notFound_shouldThrow() {
        Mockito.when(workspaceRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> workspaceService.findById(99L));
    }

    // ═══════════════════════════ listados ═══════════════════════════

    // FUNCIÓN: findAll -> lista mapeada
    @Test
    void findAll_shouldReturnList() {
        Mockito.when(workspaceRepository.findAll())
                .thenReturn(List.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Assertions.assertEquals(1, workspaceService.findAll().size());
    }

    // FUNCIÓN: findAllAvailable -> solo disponibles
    @Test
    void findAllAvailable_shouldReturnList() {
        Mockito.when(workspaceRepository.findByAvailableTrue())
                .thenReturn(List.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Assertions.assertEquals(1, workspaceService.findAllAvailable().size());
    }

    // FUNCIÓN: findByType -> por tipo y disponibles
    @Test
    void findByType_shouldReturnList() {
        Mockito.when(workspaceRepository.findByTypeAndAvailableTrue(WorkspaceType.ESCRITORIO))
                .thenReturn(List.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Assertions.assertEquals(1, workspaceService.findByType(WorkspaceType.ESCRITORIO).size());
    }

    // ═══════════════════════════ update ═══════════════════════════

    // FUNCIÓN: actualizar sin cambiar el tipo -> guarda y retorna (ArgumentCaptor)
    @Test
    void update_sameType_shouldSaveAndReturn() {
        // Arrange
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.ESCRITORIO, 4, 2);
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(workspaceRepository.findByName("Escritorio 1")).thenReturn(Optional.empty());
        Mockito.when(workspaceRepository.save(ArgumentMatchers.any(Workspace.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        workspaceService.update(10L, dto);

        // Assert
        ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
        Mockito.verify(workspaceRepository).save(captor.capture());
        Assertions.assertEquals(2, captor.getValue().getFloor());
        // No cambió el tipo, así que nunca consulta reservas
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // VALIDACIÓN: actualizar workspace inexistente -> excepción y no guarda
    @Test
    void update_notFound_shouldThrow() {
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.ESCRITORIO, 4, 1);
        Mockito.when(workspaceRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> workspaceService.update(99L, dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: nombre usado por OTRO workspace -> excepción y no guarda
    @Test
    void update_duplicateName_shouldThrow() {
        WorkspaceRequestDto dto = buildRequest("Sala A", WorkspaceType.ESCRITORIO, 4, 1);
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        // Existe otro workspace (id 99) con ese nombre
        Mockito.when(workspaceRepository.findByName("Sala A"))
                .thenReturn(Optional.of(buildWorkspace(99L, "Sala A", WorkspaceType.ESCRITORIO)));

        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.update(10L, dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: piso inválido al actualizar -> excepción y no guarda
    @Test
    void update_invalidFloor_shouldThrow() {
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.ESCRITORIO, 4, 0);
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(workspaceRepository.findByName("Escritorio 1")).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.update(10L, dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: capacidad inválida al actualizar -> excepción y no guarda
    @Test
    void update_invalidCapacity_shouldThrow() {
        WorkspaceRequestDto dto = buildRequest("Cabina 1", WorkspaceType.CABINA, 5, 1);
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Cabina 1", WorkspaceType.CABINA)));
        Mockito.when(workspaceRepository.findByName("Cabina 1")).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.update(10L, dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: cambiar el tipo con reservas activas -> excepción y no guarda
    @Test
    void update_typeChangedWithActiveBookings_shouldThrow() {
        // Arrange (era ESCRITORIO, ahora SALA_PRIVADA -> cambio de tipo)
        WorkspaceRequestDto dto = buildRequest("Escritorio 1", WorkspaceType.SALA_PRIVADA, 8, 1);
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(workspaceRepository.findByName("Escritorio 1")).thenReturn(Optional.empty());

        Booking activa = new Booking();
        activa.setStatus(BookingStatus.CONFIRMADA);
        Mockito.when(bookingRepository.findByWorkspaceIdAndStatusIn(
                        ArgumentMatchers.eq(10L), ArgumentMatchers.anyList()))
                .thenReturn(List.of(activa));

        // Act + Assert
        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.update(10L, dto));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // ═══════════════════════════ searchPage ═══════════════════════════

    // FUNCIÓN: buscar con nombre -> consulta por nombre
    @Test
    void searchPage_withName_shouldUseNameQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Workspace> page = new PageImpl<>(List.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(workspaceRepository.findByNameContainingIgnoreCase("Escri", pageable)).thenReturn(page);

        Page<WorkspaceResponseDto> result = workspaceService.searchPage("Escri", pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(workspaceRepository).findByNameContainingIgnoreCase("Escri", pageable);
        Mockito.verify(workspaceRepository, Mockito.never()).findAll(pageable);
    }

    // FUNCIÓN: buscar sin nombre -> findAll paginado
    @Test
    void searchPage_withoutName_shouldUseFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Workspace> page = new PageImpl<>(List.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(workspaceRepository.findAll(pageable)).thenReturn(page);

        Page<WorkspaceResponseDto> result = workspaceService.searchPage(null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Mockito.verify(workspaceRepository).findAll(pageable);
    }

    // ═══════════════════════════ delete ═══════════════════════════

    // FUNCIÓN: eliminar -> delega en deleteById (times + verifyNoMoreInteractions + verifyNoInteractions)
    @Test
    void delete_shouldCallDeleteById() {
        workspaceService.delete(10L);

        Mockito.verify(workspaceRepository, Mockito.times(1)).deleteById(10L);
        Mockito.verifyNoMoreInteractions(workspaceRepository);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    // ═══════════════════════════ disable ═══════════════════════════

    // FUNCIÓN: deshabilitar sin reservas activas -> guarda con available=false (ArgumentCaptor + InOrder)
    @Test
    void disable_noActiveBookings_shouldDisableAndSave() {
        // Arrange
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Mockito.when(bookingRepository.findByWorkspaceIdAndStatusIn(
                        ArgumentMatchers.eq(10L), ArgumentMatchers.anyList()))
                .thenReturn(List.of());
        Mockito.when(workspaceRepository.save(ArgumentMatchers.any(Workspace.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        workspaceService.disable(10L);

        // Assert: se guardó como no disponible
        ArgumentCaptor<Workspace> captor = ArgumentCaptor.forClass(Workspace.class);
        Mockito.verify(workspaceRepository).save(captor.capture());
        Assertions.assertFalse(captor.getValue().getAvailable());

        // Orden: buscar workspace -> consultar reservas -> guardar
        InOrder inOrder = Mockito.inOrder(workspaceRepository, bookingRepository);
        inOrder.verify(workspaceRepository).findById(10L);
        inOrder.verify(bookingRepository).findByWorkspaceIdAndStatusIn(
                ArgumentMatchers.eq(10L), ArgumentMatchers.anyList());
        inOrder.verify(workspaceRepository).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: deshabilitar con reservas activas -> excepción y no guarda
    @Test
    void disable_withActiveBookings_shouldThrow() {
        Mockito.when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(buildWorkspace(10L, "Escritorio 1", WorkspaceType.ESCRITORIO)));
        Booking activa = new Booking();
        activa.setStatus(BookingStatus.PENDIENTE);
        Mockito.when(bookingRepository.findByWorkspaceIdAndStatusIn(
                        ArgumentMatchers.eq(10L), ArgumentMatchers.anyList()))
                .thenReturn(List.of(activa));

        Assertions.assertThrows(BusinessConflictException.class, () -> workspaceService.disable(10L));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // VALIDACIÓN: deshabilitar workspace inexistente -> excepción
    @Test
    void disable_notFound_shouldThrow() {
        Mockito.when(workspaceRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> workspaceService.disable(99L));
        Mockito.verify(workspaceRepository, Mockito.never()).save(ArgumentMatchers.any(Workspace.class));
    }

    // ═══════════════════════════ Helpers ═══════════════════════════

    private WorkspaceRequestDto buildRequest(String name, WorkspaceType type, int capacity, int floor) {
        WorkspaceRequestDto dto = new WorkspaceRequestDto();
        dto.setName(name);
        dto.setType(type);
        dto.setCapacity(capacity);
        dto.setPricePerHour(5.0);
        dto.setFloor(floor);
        dto.setDescription("desc");
        return dto;
    }

    private Workspace buildWorkspace(Long id, String name, WorkspaceType type) {
        Workspace w = new Workspace();
        w.setId(id);
        w.setName(name);
        w.setType(type);
        w.setCapacity(4);
        w.setPricePerHour(5.0);
        w.setFloor(1);
        w.setAvailable(true);
        w.setDescription("desc");
        return w;
    }
}

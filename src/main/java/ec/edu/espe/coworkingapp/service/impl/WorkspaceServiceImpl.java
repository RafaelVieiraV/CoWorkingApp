package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.domain.BookingStatus;
import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import ec.edu.espe.coworkingapp.repository.BookingRepository;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import ec.edu.espe.coworkingapp.service.WorkspaceService;
import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;
import ec.edu.espe.coworkingapp.web.advice.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final BookingRepository bookingRepository;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository, BookingRepository bookingRepository) {
        this.workspaceRepository = workspaceRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public WorkspaceResponseDto create(WorkspaceRequestDto dto) {
        if (workspaceRepository.existsByName(dto.getName())) {
            throw new BusinessConflictException("Ya existe un workspace con el nombre: " + dto.getName());
        }
        
        validateCapacityForType(dto.getType(), dto.getCapacity());

        if (dto.getFloor() <= 0) {
            throw new BusinessConflictException("El piso debe ser mayor a 0");
        }

        Workspace w = new Workspace();
        w.setName(dto.getName());
        w.setType(dto.getType());
        w.setCapacity(dto.getCapacity());
        w.setPricePerHour(dto.getPricePerHour());
        w.setFloor(dto.getFloor());
        w.setDescription(dto.getDescription());
        w.setAvailable(true);

        return toResponse(workspaceRepository.save(w));
    }

    @Override
    public WorkspaceResponseDto findById(Long id) {
        Workspace w = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado con id: " + id));
        return toResponse(w);
    }

    @Override
    public List<WorkspaceResponseDto> findAll() {
        return workspaceRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponseDto> findAllAvailable() {
        return workspaceRepository.findByAvailableTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponseDto> findByType(WorkspaceType type) {
        return workspaceRepository.findByTypeAndAvailableTrue(type).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponseDto update(Long id, WorkspaceRequestDto dto) {
        Workspace w = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado con id: " + id));

        workspaceRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessConflictException("Ya existe otro workspace con el nombre: " + dto.getName());
            }
        });

        if (dto.getFloor() <= 0) {
            throw new BusinessConflictException("El piso debe ser mayor a 0");
        }
        validateCapacityForType(dto.getType(), dto.getCapacity());

        if (w.getType() != dto.getType()) {
            boolean hasActive = bookingRepository.findByWorkspaceIdAndStatusIn(id, List.of(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA))
                    .stream().anyMatch(b -> b.getStatus() == BookingStatus.PENDIENTE || b.getStatus() == BookingStatus.CONFIRMADA);
            if (hasActive) {
                throw new BusinessConflictException("No se puede cambiar el tipo porque hay reservas activas en este workspace.");
            }
        }

        w.setName(dto.getName());
        w.setType(dto.getType());
        w.setCapacity(dto.getCapacity());
        w.setPricePerHour(dto.getPricePerHour());
        w.setFloor(dto.getFloor());
        w.setDescription(dto.getDescription());

        return toResponse(workspaceRepository.save(w));
    }

    @Override
    public void disable(Long id) {
        Workspace w = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado con id: " + id));

        boolean hasActive = bookingRepository.findByWorkspaceIdAndStatusIn(id, List.of(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA))
                .isEmpty() == false;

        if (hasActive) {
            throw new BusinessConflictException("El workspace tiene reservas activas, cancélalas antes de deshabilitarlo");
        }

        w.setAvailable(false);
        workspaceRepository.save(w);
    }

    private void validateCapacityForType(WorkspaceType type, int capacity) {
        boolean valid = true;
        String msg = "";
        if (type == WorkspaceType.CABINA) { if(capacity > 2) { valid = false; msg = "Cabina máximo 2 personas"; } }
        else if (type == WorkspaceType.ESCRITORIO) { if(capacity > 4) { valid = false; msg = "Escritorio máximo 4 personas"; } }
        else if (type == WorkspaceType.SALA_PRIVADA) { if(capacity > 10) { valid = false; msg = "Sala Privada máximo 10 personas"; } }
        else if (type == WorkspaceType.SALA_REUNION) { if(capacity < 4) { valid = false; msg = "Sala de Reunión mínimo 4 personas"; } }
        
        if (!valid) throw new BusinessConflictException(msg);
    }

    private WorkspaceResponseDto toResponse(Workspace w) {
        WorkspaceResponseDto res = new WorkspaceResponseDto();
        res.setId(w.getId());
        res.setName(w.getName());
        res.setType(w.getType());
        res.setCapacity(w.getCapacity());
        res.setPricePerHour(w.getPricePerHour());
        res.setFloor(w.getFloor());
        res.setAvailable(w.getAvailable());
        res.setDescription(w.getDescription());
        return res;
    }
}
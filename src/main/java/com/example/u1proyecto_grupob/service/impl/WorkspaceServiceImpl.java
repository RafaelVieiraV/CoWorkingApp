package com.example.u1proyecto_grupob.service.impl;

import com.example.u1proyecto_grupob.domain.Workspace;
import com.example.u1proyecto_grupob.domain.WorkspaceType;
import com.example.u1proyecto_grupob.domain.BookingStatus;
import com.example.u1proyecto_grupob.dto.WorkspaceRequestDto;
import com.example.u1proyecto_grupob.dto.WorkspaceResponseDto;
import com.example.u1proyecto_grupob.repository.WorkspaceRepository;
import com.example.u1proyecto_grupob.repository.BookingRepository;
import com.example.u1proyecto_grupob.service.WorkspaceService;
import com.example.u1proyecto_grupob.web.advice.BusinessConflictException;
import com.example.u1proyecto_grupob.web.advice.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
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
            throw new BusinessConflictException("El nombre del workspace ya existe: " + dto.getName());
        }

        validateCapacity(dto.getType(), dto.getCapacity());
        if (dto.getFloor() <= 0) {
            throw new BusinessConflictException("El piso debe ser mayor que 0");
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
        return workspaceRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponseDto> findAllAvailable() {
        return workspaceRepository.findByAvailableTrue().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponseDto> findByType(WorkspaceType type) {
        return workspaceRepository.findByType(type).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponseDto update(Long id, WorkspaceRequestDto dto) {
        Workspace w = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace no encontrado con id: " + id));

        workspaceRepository.findByName(dto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessConflictException("El nombre ya pertenece a otro workspace");
            }
        });

        validateCapacity(dto.getType(), dto.getCapacity());
        if (dto.getFloor() <= 0) {
            throw new BusinessConflictException("El piso debe ser mayor que 0");
        }

        if (w.getType() != dto.getType()) {
            boolean hasActive = !bookingRepository.findByWorkspaceIdAndStatusIn(id, Arrays.asList(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA)).isEmpty();
            if (hasActive) {
                throw new BusinessConflictException("No se puede cambiar el tipo si tiene reservas activas");
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

        boolean hasActive = !bookingRepository.findByWorkspaceIdAndStatusIn(id, Arrays.asList(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA)).isEmpty();
        if (hasActive) {
            throw new BusinessConflictException("El workspace tiene reservas activas, cancélalas antes de deshabilitarlo");
        }

        w.setAvailable(false);
        workspaceRepository.save(w);
    }

    private void validateCapacity(WorkspaceType type, Integer capacity) {
        switch (type) {
            case CABINA: if (capacity > 2) throw new BusinessConflictException("Cabina no debe superar 2"); break;
            case ESCRITORIO: if (capacity > 4) throw new BusinessConflictException("Escritorio no debe superar 4"); break;
            case SALA_PRIVADA: if (capacity > 10) throw new BusinessConflictException("Sala Privada no debe superar 10"); break;
            case SALA_REUNION: if (capacity < 4) throw new BusinessConflictException("Sala Reunión mínimo 4"); break;
        }
    }

    private WorkspaceResponseDto toResponse(Workspace w) {
        WorkspaceResponseDto dto = new WorkspaceResponseDto();
        dto.setId(w.getId());
        dto.setName(w.getName());
        dto.setType(w.getType());
        dto.setCapacity(w.getCapacity());
        dto.setPricePerHour(w.getPricePerHour());
        dto.setFloor(w.getFloor());
        dto.setAvailable(w.getAvailable());
        dto.setDescription(w.getDescription());
        return dto;
    }
}

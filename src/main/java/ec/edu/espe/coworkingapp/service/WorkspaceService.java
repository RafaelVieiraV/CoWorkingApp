package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import java.util.List;

public interface WorkspaceService {
    WorkspaceResponseDto create(WorkspaceRequestDto dto);
    WorkspaceResponseDto findById(Long id);
    List<WorkspaceResponseDto> findAll();
    List<WorkspaceResponseDto> findAllAvailable();
    WorkspaceResponseDto update(Long id, WorkspaceRequestDto dto);
    void disable(Long id);
    List<WorkspaceResponseDto> findByType(WorkspaceType type);
}
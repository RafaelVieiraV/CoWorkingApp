package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface WorkspaceService {
    WorkspaceResponseDto create(WorkspaceRequestDto dto);
    WorkspaceResponseDto findById(Long id);
    List<WorkspaceResponseDto> findAll();
    Page<WorkspaceResponseDto> searchPage(String name, Boolean available, Pageable pageable);
    WorkspaceResponseDto update(Long id, WorkspaceRequestDto dto);
    void delete(Long id);
    List<WorkspaceResponseDto> findAllAvailable();
    List<WorkspaceResponseDto> findByType(WorkspaceType type);
    void disable(Long id);
    void activate(Long id);
}
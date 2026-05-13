package com.example.u1proyecto_grupob.service;

import com.example.u1proyecto_grupob.domain.WorkspaceType;
import com.example.u1proyecto_grupob.dto.WorkspaceRequestDto;
import com.example.u1proyecto_grupob.dto.WorkspaceResponseDto;
import java.util.List;

public interface WorkspaceService {
    WorkspaceResponseDto create(WorkspaceRequestDto dto);
    WorkspaceResponseDto findById(Long id);
    List<WorkspaceResponseDto> findAll();
    List<WorkspaceResponseDto> findAllAvailable();
    List<WorkspaceResponseDto> findByType(WorkspaceType type);
    WorkspaceResponseDto update(Long id, WorkspaceRequestDto dto);
    void disable(Long id);
}

package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequest;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponse;
import java.util.List;

public interface WorkspaceService {
    List<WorkspaceResponse> findAll();
    List<WorkspaceResponse> findAvailable();
    WorkspaceResponse findById(Long id);
    WorkspaceResponse create(WorkspaceRequest request);
    WorkspaceResponse update(Long id, WorkspaceRequest request);
    void delete(Long id);
}
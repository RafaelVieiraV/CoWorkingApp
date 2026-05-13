package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.domain.Workspace;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequest;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponse;
import ec.edu.espe.coworkingapp.repository.WorkspaceRepository;
import ec.edu.espe.coworkingapp.service.WorkspaceService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<WorkspaceResponse> findAll() {
        return workspaceRepository.findAll()
                .stream()
                .map(WorkspaceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponse> findAvailable() {
        return workspaceRepository.findByAvailableTrue()
                .stream()
                .map(WorkspaceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponse findById(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace no encontrado con id: " + id));
        return WorkspaceResponse.from(workspace);
    }

    @Override
    public WorkspaceResponse create(WorkspaceRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setType(request.getType());
        workspace.setCapacity(request.getCapacity());
        workspace.setPricePerHour(request.getPricePerHour());
        workspace.setFloor(request.getFloor());
        workspace.setDescription(request.getDescription());
        return WorkspaceResponse.from(workspaceRepository.save(workspace));
    }

    @Override
    public WorkspaceResponse update(Long id, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace no encontrado con id: " + id));
        workspace.setName(request.getName());
        workspace.setType(request.getType());
        workspace.setCapacity(request.getCapacity());
        workspace.setPricePerHour(request.getPricePerHour());
        workspace.setFloor(request.getFloor());
        workspace.setDescription(request.getDescription());
        return WorkspaceResponse.from(workspaceRepository.save(workspace));
    }

    @Override
    public void delete(Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new RuntimeException("Workspace no encontrado con id: " + id);
        }
        workspaceRepository.deleteById(id);
    }
}
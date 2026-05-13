package com.example.u1proyecto_grupob.repository;

import com.example.u1proyecto_grupob.domain.Workspace;
import com.example.u1proyecto_grupob.domain.WorkspaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByName(String name);
    boolean existsByName(String name);
    List<Workspace> findByAvailableTrue();
    List<Workspace> findByType(WorkspaceType type);
    List<Workspace> findByTypeAndAvailableTrue(WorkspaceType type);
}

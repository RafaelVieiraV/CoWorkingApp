package ec.edu.espe.coworkingapp.repository;

import ec.edu.espe.coworkingapp.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByAvailableTrue();
    List<Workspace> findByType(Workspace.WorkspaceType type);
}
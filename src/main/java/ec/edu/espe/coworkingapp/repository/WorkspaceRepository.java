package ec.edu.espe.coworkingapp.repository;



import ec.edu.espe.coworkingapp.domain.Workspace;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;



@Repository

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByName(String name);
    boolean existsByName(String name);
    List<Workspace> findByAvailableTrue();
    List<Workspace> findByType(WorkspaceType type);
    List<Workspace> findByTypeAndAvailableTrue(WorkspaceType type);

    Page<Workspace> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Workspace> findByNameContainingIgnoreCaseAndAvailable(String name, Boolean available, Pageable pageable);

    Page<Workspace> findByAvailable(Boolean available, Pageable pageable);

    @Query("SELECT w FROM Workspace w " +
           "WHERE (:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', CAST(:name as string), '%'))) " +
           "AND (:available IS NULL OR w.available = :available) " +
           "AND (:type IS NULL OR w.type = :type)")
    Page<Workspace> searchWorkspaces(
            @Param("name") String name,
            @Param("available") Boolean available,
            @Param("type") WorkspaceType type,
            Pageable pageable);
}

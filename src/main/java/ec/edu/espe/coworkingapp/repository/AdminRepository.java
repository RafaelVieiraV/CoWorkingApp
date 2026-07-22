package ec.edu.espe.coworkingapp.repository;

import ec.edu.espe.coworkingapp.domain.Admins;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admins, Long> {
    Optional<Admins> findByEmail(String email);
}

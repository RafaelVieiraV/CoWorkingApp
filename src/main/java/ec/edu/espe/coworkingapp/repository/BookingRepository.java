package ec.edu.espe.coworkingapp.repository;

import ec.edu.espe.coworkingapp.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByMemberId(Long memberId);
    List<Booking> findByWorkspaceId(Long workspaceId);
    List<Booking> findByStatus(Booking.BookingStatus status);
}
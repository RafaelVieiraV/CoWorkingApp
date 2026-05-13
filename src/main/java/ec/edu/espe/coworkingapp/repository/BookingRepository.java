package ec.edu.espe.coworkingapp.repository;

import ec.edu.espe.coworkingapp.domain.Booking;
import ec.edu.espe.coworkingapp.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByMemberId(Long memberId);
    List<Booking> findByWorkspaceId(Long workspaceId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByWorkspaceIdAndStatus(Long workspaceId, BookingStatus status);
    List<Booking> findByMemberIdAndStatusNot(Long memberId, BookingStatus status);
    List<Booking> findByWorkspaceIdAndStatusIn(Long workspaceId, List<BookingStatus> statuses);
    List<Booking> findByMemberIdAndStatusNotAndStartDatetimeBetween(Long memberId, BookingStatus status, LocalDateTime start, LocalDateTime end);
}
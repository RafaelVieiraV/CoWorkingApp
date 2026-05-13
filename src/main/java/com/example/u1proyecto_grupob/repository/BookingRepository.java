package com.example.u1proyecto_grupob.repository;

import com.example.u1proyecto_grupob.domain.Booking;
import com.example.u1proyecto_grupob.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByMemberId(Long memberId);
    List<Booking> findByWorkspaceId(Long workspaceId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByWorkspaceIdAndStatus(Long workspaceId, BookingStatus status);

    List<Booking> findByMemberIdAndStatusNot(Long memberId, BookingStatus status);
    List<Booking> findByWorkspaceIdAndStatusIn(Long workspaceId, List<BookingStatus> statuses);
    List<Booking> findByMemberIdAndStatusNotAndStartDatetimeBetween(Long memberId, BookingStatus status, LocalDateTime start, LocalDateTime end);
}

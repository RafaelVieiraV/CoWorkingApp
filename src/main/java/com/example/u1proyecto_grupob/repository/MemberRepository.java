package com.example.u1proyecto_grupob.repository;

import com.example.u1proyecto_grupob.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Member> findByActiveTrue();
}

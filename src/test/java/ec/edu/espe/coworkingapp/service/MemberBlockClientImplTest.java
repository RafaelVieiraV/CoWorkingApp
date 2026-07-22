package ec.edu.espe.coworkingapp.service;
import ec.edu.espe.coworkingapp.domain.Member;
import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.service.impl.MemberBlockClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class MemberBlockClientImplTest {

    private MemberBlockClientImpl memberBlockClient;
    private MemberRepository memberRepository;

    @BeforeEach
    public void setUp() {
        memberRepository = Mockito.mock(MemberRepository.class);
        memberBlockClient = new MemberBlockClientImpl(memberRepository);
    }

    // Miembro bloqueado -> retorna true
    @Test
    void isBlocked_memberBlocked_shouldReturnTrue() {
        // Arrange
        String email = "jjguerra5@espe.edu.ec";
        Member member = new Member();
        member.setEmail(email);
        member.setBlocked(true);
        Mockito.when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // Act
        boolean result = memberBlockClient.isBlocked(email);

        // Assert
        Assertions.assertTrue(result);
        Mockito.verify(memberRepository).findByEmail(email);
    }

    // Miembro NO bloqueado -> retorna false
    @Test
    void isBlocked_memberNotBlocked_shouldReturnFalse() {
        // Arrange
        String email = "jjguerra5@espe.edu.ec";
        Member member = new Member();
        member.setEmail(email);
        member.setBlocked(false);
        Mockito.when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // Act
        boolean result = memberBlockClient.isBlocked(email);

        // Assert
        Assertions.assertFalse(result);
        Mockito.verify(memberRepository).findByEmail(email);
    }

    // Miembro no existe -> retorna false
    @Test
    void isBlocked_memberNotFound_shouldReturnFalse() {
        // Arrange
        String email = "noexiste@espe.edu.ec";
        Mockito.when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = memberBlockClient.isBlocked(email);

        // Assert
        Assertions.assertFalse(result);
        Mockito.verify(memberRepository).findByEmail(email);
    }

    // Email null -> retorna false y NO consulta el repositorio
    @Test
    void isBlocked_nullEmail_shouldReturnFalse_andNotCallRepository() {
        // Act
        boolean result = memberBlockClient.isBlocked(null);

        // Assert
        Assertions.assertFalse(result);
        Mockito.verifyNoInteractions(memberRepository);
    }
}
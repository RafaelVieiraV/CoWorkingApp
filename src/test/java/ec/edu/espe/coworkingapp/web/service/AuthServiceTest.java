package ec.edu.espe.coworkingapp.web.service;

import ec.edu.espe.coworkingapp.domain.Admins;
import ec.edu.espe.coworkingapp.repository.AdminRepository;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import ec.edu.espe.coworkingapp.service.impl.AuthServiceImpl;
import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;
import ec.edu.espe.coworkingapp.web.advice.InvalidCredentialsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class AuthServiceTest {
    private AuthServiceImpl authService;
    private AdminRepository adminRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;

    // Arrange comun de todas las pruebas
    @BeforeEach
    public void setUp() {
        adminRepository = Mockito.mock(AdminRepository.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        authService = new AuthServiceImpl(adminRepository, jwtUtil, passwordEncoder);
    }

    @Test
    void login_validCredentials_shouldReturnToken() {
        // Arrange
        String email = "admin@GrupoB.com";
        String password = "password123_GrupoB";
        String encodedPassword = "encodedPassword123_GrupoB";
        String expectedToken = "jwt-token-valido_GrupoB";

        Admins admin = new Admins();
        admin.setEmail(email);
        admin.setPassword(encodedPassword);

        Mockito.when(adminRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        Mockito.when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        Mockito.when(jwtUtil.generateToken(email)).thenReturn(expectedToken);

        // Act
        String actualToken = authService.login(email, password);

        // Assert
        Assertions.assertEquals(expectedToken, actualToken);
        Mockito.verify(adminRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).matches(password, encodedPassword);
        Mockito.verify(jwtUtil).generateToken(email);
    }

    @Test
    void login_emailNotFound_shouldThrowException_andNotCallDependencies() {
        // Arrange
        String email = "no-existe@GrupoB.com";
        String password = "password123_GrupoB";

        Mockito.when(adminRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act + Assert
        InvalidCredentialsException ex = Assertions.assertThrows(InvalidCredentialsException.class, ()
                -> authService.login(email, password));

        Assertions.assertEquals("Credenciales inválidas", ex.getMessage());
        Mockito.verify(adminRepository).findByEmail(email);

        // No debe llamar a las otras dependencias porque falla en la búsqueda del usuario
        Mockito.verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void login_invalidPassword_shouldThrowException_andNotGenerateToken() {
        // Arrange
        String email = "admin@GrupoB.com";
        String password = "wrong-password_GrupoB";
        String encodedPassword = "encodedPassword123_GrupoB";

        Admins admin = new Admins();
        admin.setEmail(email);
        admin.setPassword(encodedPassword);

        Mockito.when(adminRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        Mockito.when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // Act + Assert
        InvalidCredentialsException ex = Assertions.assertThrows(InvalidCredentialsException.class, ()
                -> authService.login(email, password));

        Assertions.assertEquals("Credenciales inválidas", ex.getMessage());
        Mockito.verify(adminRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).matches(password, encodedPassword);
        Mockito.verifyNoInteractions(jwtUtil);
    }

    @Test
    void register_validData_shouldEncodePasswordAndSave_usingCaptor() {
        // Arrange
        String email = "nuevo.admin@GrupoB.com";
        String password = "password123_GrupoB";
        String encodedPassword = "encodedPassword123_GrupoB";

        Mockito.when(adminRepository.findByEmail(email)).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        ArgumentCaptor<Admins> captor = ArgumentCaptor.forClass(Admins.class);

        // Act
        authService.register(email, password);

        // Assert
        Mockito.verify(adminRepository).findByEmail(email);
        Mockito.verify(passwordEncoder).encode(password);
        Mockito.verify(adminRepository).save(captor.capture());

        Admins savedAdmin = captor.getValue();
        Assertions.assertEquals(email, savedAdmin.getEmail());
        Assertions.assertEquals(encodedPassword, savedAdmin.getPassword());
    }

    @Test
    void register_emailAlreadyExists_shouldThrowException_andNotSave() {
        // Arrange
        String email = "registrado@GrupoB.com";
        String password = "password123_GrupoB";

        Admins existingAdmin = new Admins();
        existingAdmin.setEmail(email);

        Mockito.when(adminRepository.findByEmail(email)).thenReturn(Optional.of(existingAdmin));

        // Act + Assert
        BusinessConflictException ex = Assertions.assertThrows(BusinessConflictException.class, ()
                -> authService.register(email, password));

        Assertions.assertEquals("El email ya está registrado", ex.getMessage());

        Mockito.verify(adminRepository).findByEmail(email);
        Mockito.verify(adminRepository, Mockito.never()).save(ArgumentMatchers.any(Admins.class));
        Mockito.verifyNoInteractions(passwordEncoder);
    }
}

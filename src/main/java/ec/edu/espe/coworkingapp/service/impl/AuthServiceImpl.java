package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.domain.Admins;
import ec.edu.espe.coworkingapp.repository.AdminRepository;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import ec.edu.espe.coworkingapp.service.AuthService;
import ec.edu.espe.coworkingapp.web.advice.BusinessConflictException;
import ec.edu.espe.coworkingapp.web.advice.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AdminRepository adminRepository,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String login(String email, String password) {
        Admins admin = adminRepository.findByEmail(email)
                .filter(a -> passwordEncoder.matches(password, a.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        return jwtUtil.generateToken(admin.getEmail());
    }

    @Override
    public void register(String email, String password) {
        if (adminRepository.findByEmail(email).isPresent()) {
            throw new BusinessConflictException("El email ya está registrado");
        }

        Admins admin = new Admins();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(admin);
    }
}
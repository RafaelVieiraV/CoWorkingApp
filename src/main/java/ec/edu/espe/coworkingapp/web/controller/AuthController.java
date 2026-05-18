package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.dto.request.AdminRequestDto;
import ec.edu.espe.coworkingapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse; // <--- Importante
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody AdminRequestDto request,
            HttpServletResponse response) { // <--- Inyectamos la respuesta HTTP

        String token = authService.login(request.getEmail(), request.getPassword());

        // 1. CREAR LA COOKIE CON EL NOMBRE "jwt" (tal como lo busca tu filtro)
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);   // Bloquea accesos de scripts maliciosos (XSS)
        jwtCookie.setSecure(true);     // Requerido en producción/Render (HTTPS)
        jwtCookie.setPath("/");        // Hace que la cookie sea válida para todo el dominio
        jwtCookie.setMaxAge(86400);    // Tiempo de vida: 1 día en segundos

        // 2. Añadir la cookie a la respuesta que va hacia el cliente
        response.addCookie(jwtCookie);

        // Mantenemos el retorno del JSON para no romper compatibilidades
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AdminRequestDto request) {
        authService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Admin creado correctamente"));
    }
}
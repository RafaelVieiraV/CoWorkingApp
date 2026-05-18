package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.dto.request.AdminRequestDto;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import ec.edu.espe.coworkingapp.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private AdminRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminRequestDto();
        validRequest.setEmail("admin@coworking.com");
        validRequest.setPassword("Val1d!Password123");
    }

    @Test
    void login_returnsTokenAndCookie() throws Exception {
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fakeToken";

        when(authService.login(anyString(), anyString())).thenReturn(fakeToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().value("jwt", fakeToken))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.email").value("admin@coworking.com"));
    }

    @Test
    void register_createsAdmin() throws Exception {
        doNothing().when(authService).register(anyString(), anyString());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Admin creado correctamente"));
    }
}


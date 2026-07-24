package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.domain.PlanType;
import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import ec.edu.espe.coworkingapp.service.MemberService;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.coworkingapp.web.MemberController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private MemberRequestDto memberRequestDto;

    // Arrange común de todas las pruebas
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        memberRequestDto = new MemberRequestDto();
        memberRequestDto.setFullName("John Doe");
        memberRequestDto.setEmail("john@example.com");
        memberRequestDto.setPhone("0912345678");
        memberRequestDto.setPlanType(PlanType.BASICO);
        memberRequestDto.setMonthlyHoursQuota(10);
    }

    @Test
    void createMember() throws Exception {
        // Arrange
        when(memberService.create(any())).thenReturn(new MemberResponseDto());

        // Act + Assert
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void searchMembers() throws Exception {
        // Arrange
        when(memberService.searchPage(any(), any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act + Assert
        mockMvc.perform(get("/api/members/search?name=John&size=5"))
                .andExpect(status().isOk());
    }

    @Test
    void findById() throws Exception {
        // Arrange
        when(memberService.findById(1L)).thenReturn(new MemberResponseDto());

        // Act + Assert
        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateMember() throws Exception {
        // Arrange
        memberRequestDto.setFullName("John Updated");
        memberRequestDto.setPlanType(PlanType.ESTANDAR);
        memberRequestDto.setMonthlyHoursQuota(20);

        when(memberService.update(eq(1L), any())).thenReturn(new MemberResponseDto());

        // Act + Assert
        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMember() throws Exception {
        // Act + Assert
        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());
    }
}
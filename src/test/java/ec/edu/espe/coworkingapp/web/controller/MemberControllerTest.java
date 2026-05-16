package ec.edu.espe.coworkingapp.web.controller;
import ec.edu.espe.coworkingapp.domain.PlanType;
import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import ec.edu.espe.coworkingapp.service.MemberService;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void createMember() throws Exception {
        MemberRequestDto req = new MemberRequestDto();
        req.setFullName("John Doe");
        req.setEmail("john@example.com");
        req.setPhone("1234567890");
        req.setPlanType(PlanType.BASICO);
        req.setMonthlyHoursQuota(10);
        when(memberService.create(any())).thenReturn(new MemberResponseDto());
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
    @Test
    void searchMembers() throws Exception {
        when(memberService.searchPage(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/members/search?name=John&size=5"))
               .andExpect(status().isOk());
    }
    @Test
    void findById() throws Exception {
        when(memberService.findById(1L)).thenReturn(new MemberResponseDto());
        mockMvc.perform(get("/api/members/1"))
               .andExpect(status().isOk());
    }
    @Test
    void updateMember() throws Exception {
        MemberRequestDto req = new MemberRequestDto();
        req.setFullName("John Updated");
        req.setEmail("john@example.com");
        req.setPhone("1234567890");
        req.setPlanType(PlanType.ESTANDAR);
        req.setMonthlyHoursQuota(20);
        when(memberService.update(eq(1L), any())).thenReturn(new MemberResponseDto());
        mockMvc.perform(put("/api/members/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    @Test
    void deleteMember() throws Exception {
        mockMvc.perform(delete("/api/members/1"))
               .andExpect(status().isNoContent());
    }
}
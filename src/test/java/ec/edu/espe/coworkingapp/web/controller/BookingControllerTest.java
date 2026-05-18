package ec.edu.espe.coworkingapp.web.controller;
import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    //private BookingService bookingService;
    @MockBean
    private JwtUtil jwtUtil;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Test
    void createBooking() throws Exception {
        BookingRequestDto req = new BookingRequestDto();
        req.setMemberId(1L);
        req.setWorkspaceId(1L);
        req.setStartDatetime(LocalDateTime.now().plusDays(1));
        req.setEndDatetime(LocalDateTime.now().plusDays(1).plusHours(2));
        when(bookingService.create(any())).thenReturn(new BookingResponseDto());
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
    @Test
    void searchBookings() throws Exception {
        when(bookingService.searchPage(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/bookings/search?id=1&size=5"))
               .andExpect(status().isOk());
    }
    @Test
    void findById() throws Exception {
        when(bookingService.findById(1L)).thenReturn(new BookingResponseDto());
        mockMvc.perform(get("/api/bookings/1"))
               .andExpect(status().isOk());
    }
    @Test
    void updateBooking() throws Exception {
        BookingRequestDto req = new BookingRequestDto();
        req.setMemberId(1L);
        req.setWorkspaceId(1L);
        req.setStartDatetime(LocalDateTime.now().plusDays(2));
        req.setEndDatetime(LocalDateTime.now().plusDays(2).plusHours(2));
        when(bookingService.update(eq(1L), any())).thenReturn(new BookingResponseDto());
        mockMvc.perform(put("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    @Test
    void deleteBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/1"))
               .andExpect(status().isNoContent());
    }
}
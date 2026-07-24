package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ec.edu.espe.coworkingapp.pubsub.BookingPipelineService;
import ec.edu.espe.coworkingapp.reactive.service.BookingEventStreamService;
import ec.edu.espe.coworkingapp.web.BookingController;
import org.junit.jupiter.api.BeforeEach;
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
    private BookingService bookingService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private BookingPipelineService bookingPipelineService;

    @MockBean
    private BookingEventStreamService bookingEventStreamService;

    private ObjectMapper objectMapper;
    private BookingRequestDto bookingRequestDto;

    // Arrange común de todas las pruebas
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setMemberId(1L);
        bookingRequestDto.setWorkspaceId(1L);
        bookingRequestDto.setStartDatetime(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEndDatetime(LocalDateTime.now().plusDays(1).plusHours(2));
    }

    @Test
    void createBooking() throws Exception {
        // Arrange
        when(bookingService.create(any())).thenReturn(new BookingResponseDto());

        // Act + Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void searchBookings() throws Exception {
        // Arrange
        when(bookingService.searchPage(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act + Assert
        mockMvc.perform(get("/api/bookings/search?id=1&size=5"))
                .andExpect(status().isOk());
    }

    @Test
    void findById() throws Exception {
        // Arrange
        when(bookingService.findById(1L)).thenReturn(new BookingResponseDto());

        // Act + Assert
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBooking() throws Exception {
        // Arrange
        bookingRequestDto.setStartDatetime(LocalDateTime.now().plusDays(2));
        bookingRequestDto.setEndDatetime(LocalDateTime.now().plusDays(2).plusHours(2));

        when(bookingService.update(eq(1L), any())).thenReturn(new BookingResponseDto());

        // Act + Assert
        mockMvc.perform(put("/api/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBooking() throws Exception {
        // Act + Assert
        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isNoContent());
    }
}
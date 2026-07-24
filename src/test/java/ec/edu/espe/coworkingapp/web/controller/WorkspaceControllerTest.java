package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import ec.edu.espe.coworkingapp.service.WorkspaceService;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.coworkingapp.web.WorkspaceController;
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

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkspaceService workspaceService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private WorkspaceRequestDto workspaceRequestDto;

    // Arrange común de todas las pruebas
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        workspaceRequestDto = new WorkspaceRequestDto();
        workspaceRequestDto.setName("Sala A");
        workspaceRequestDto.setType(WorkspaceType.SALA_REUNION);
        workspaceRequestDto.setCapacity(5);
        workspaceRequestDto.setPricePerHour(10.0);
        workspaceRequestDto.setFloor(1);
    }

    @Test
    void createWorkspace() throws Exception {
        // Arrange
        when(workspaceService.create(any())).thenReturn(new WorkspaceResponseDto());

        // Act + Assert
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workspaceRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void searchWorkspaces() throws Exception {
        // Arrange
        when(workspaceService.searchPage(any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act + Assert
        mockMvc.perform(get("/api/workspaces/search?name=Sala&size=5"))
                .andExpect(status().isOk());
    }

    @Test
    void findById() throws Exception {
        // Arrange
        when(workspaceService.findById(1L)).thenReturn(new WorkspaceResponseDto());

        // Act + Assert
        mockMvc.perform(get("/api/workspaces/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateWorkspace() throws Exception {
        // Arrange
        workspaceRequestDto.setName("Sala B");
        workspaceRequestDto.setCapacity(6);
        workspaceRequestDto.setPricePerHour(12.0);

        when(workspaceService.update(eq(1L), any())).thenReturn(new WorkspaceResponseDto());

        // Act + Assert
        mockMvc.perform(put("/api/workspaces/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workspaceRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteWorkspace() throws Exception {
        // Act + Assert
        mockMvc.perform(delete("/api/workspaces/1"))
                .andExpect(status().isNoContent());
    }
}
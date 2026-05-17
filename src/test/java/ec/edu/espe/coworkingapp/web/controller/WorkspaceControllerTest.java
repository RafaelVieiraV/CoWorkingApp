package ec.edu.espe.coworkingapp.web.controller;
import ec.edu.espe.coworkingapp.domain.WorkspaceType;
import ec.edu.espe.coworkingapp.dto.request.WorkspaceRequestDto;
import ec.edu.espe.coworkingapp.dto.response.WorkspaceResponseDto;
import ec.edu.espe.coworkingapp.service.WorkspaceService;
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
@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WorkspaceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WorkspaceService workspaceService;
    @MockBean
    private JwtUtil jwtUtil;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void createWorkspace() throws Exception {
        WorkspaceRequestDto req = new WorkspaceRequestDto();
        req.setName("Sala A");
        req.setType(WorkspaceType.SALA_REUNION);
        req.setCapacity(5);
        req.setPricePerHour(10.0);
        req.setFloor(1);
        when(workspaceService.create(any())).thenReturn(new WorkspaceResponseDto());
        mockMvc.perform(post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
    @Test
    void searchWorkspaces() throws Exception {
        when(workspaceService.searchPage(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/workspaces/search?name=Sala&size=5"))
               .andExpect(status().isOk());
    }
    @Test
    void findById() throws Exception {
        when(workspaceService.findById(1L)).thenReturn(new WorkspaceResponseDto());
        mockMvc.perform(get("/api/workspaces/1"))
               .andExpect(status().isOk());
    }
    @Test
    void updateWorkspace() throws Exception {
        WorkspaceRequestDto req = new WorkspaceRequestDto();
        req.setName("Sala B");
        req.setType(WorkspaceType.SALA_REUNION);
        req.setCapacity(6);
        req.setPricePerHour(12.0);
        req.setFloor(1);
        when(workspaceService.update(eq(1L), any())).thenReturn(new WorkspaceResponseDto());
        mockMvc.perform(put("/api/workspaces/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    @Test
    void deleteWorkspace() throws Exception {
        mockMvc.perform(delete("/api/workspaces/1"))
               .andExpect(status().isNoContent());
    }
}
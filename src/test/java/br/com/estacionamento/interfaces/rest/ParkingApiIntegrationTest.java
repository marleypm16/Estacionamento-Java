package br.com.estacionamento.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "parking.admin-token=test-admin-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParkingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCompleteEntryOccupancyAndExitFlow() throws Exception {
        mockMvc.perform(post("/api/v1/stays/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"ABC1D23\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/stays/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"abc1d23\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/stays/occupancy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupied").value(1));

        mockMvc.perform(post("/api/v1/stays/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"ABC1D23\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.amountCharged").value(8.00));

        mockMvc.perform(get("/api/v1/stays")
                        .param("status", "FINISHED")
                        .header("X-Admin-Token", "test-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plate").value("ABC1D23"));
    }

    @Test
    void shouldRejectHistoryWithoutTheAdministrativeToken() throws Exception {
        mockMvc.perform(get("/api/v1/stays"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso administrativo necessário para consultar o histórico"));
    }
}

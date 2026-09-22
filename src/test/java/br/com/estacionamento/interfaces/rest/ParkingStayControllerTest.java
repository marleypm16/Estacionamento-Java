package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.service.ParkingService;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingStayController.class)
class ParkingStayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingService parkingService;

    @MockitoBean
    private AdministrativeAccess administrativeAccess;

    @Test
    void shouldReturnCreatedForValidEntry() throws Exception {
        UUID id = UUID.randomUUID();
        ParkingStay stay = ParkingStay.start(
                id,
                LicensePlate.of("ABC1D23"),
                Instant.parse("2026-09-21T10:00:00Z"));
        when(parkingService.registerEntry("ABC1D23")).thenReturn(stay);

        mockMvc.perform(post("/api/v1/stays/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"ABC1D23\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/stays/" + id))
                .andExpect(jsonPath("$.plate").value("ABC1D23"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnBadRequestForBlankPlate() throws Exception {
        mockMvc.perform(post("/api/v1/stays/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.plate").value("A placa é obrigatória"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPlate() throws Exception {
        when(parkingService.registerEntry(anyString()))
                .thenThrow(new IllegalArgumentException("Placa inválida"));

        mockMvc.perform(post("/api/v1/stays/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"INVALIDA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Placa inválida"));
    }
}

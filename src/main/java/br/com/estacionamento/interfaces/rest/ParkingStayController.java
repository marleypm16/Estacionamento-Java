package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.service.ParkingService;
import br.com.estacionamento.domain.model.StayStatus;
import br.com.estacionamento.interfaces.rest.dto.OccupancyResponse;
import br.com.estacionamento.interfaces.rest.dto.ParkingStayResponse;
import br.com.estacionamento.interfaces.rest.dto.RegisterEntryRequest;
import br.com.estacionamento.interfaces.rest.dto.RegisterExitRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stays")
public class ParkingStayController {

    private final ParkingService parkingService;

    public ParkingStayController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping("/entries")
    public ResponseEntity<ParkingStayResponse> registerEntry(
            @Valid @RequestBody RegisterEntryRequest request) {
        ParkingStayResponse response = ParkingStayResponse.from(parkingService.registerEntry(request.plate()));
        return ResponseEntity.created(URI.create("/api/v1/stays/" + response.id())).body(response);
    }

    @PostMapping("/exits")
    public ParkingStayResponse registerExit(@Valid @RequestBody RegisterExitRequest request) {
        return ParkingStayResponse.from(parkingService.registerExit(request.plate()));
    }

    @GetMapping
    public List<ParkingStayResponse> listAll(
            @RequestParam(required = false) String plate,
            @RequestParam(required = false) StayStatus status) {
        return parkingService.search(plate, status).stream().map(ParkingStayResponse::from).toList();
    }

    @GetMapping("/active")
    public List<ParkingStayResponse> listActive() {
        return parkingService.listActive().stream().map(ParkingStayResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ParkingStayResponse findById(@PathVariable UUID id) {
        return ParkingStayResponse.from(parkingService.findById(id));
    }

    @GetMapping("/occupancy")
    public OccupancyResponse getOccupancy() {
        return OccupancyResponse.from(parkingService.getOccupancy());
    }
}

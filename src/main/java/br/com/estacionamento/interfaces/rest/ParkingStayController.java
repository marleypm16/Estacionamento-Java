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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stays")
public class ParkingStayController {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    private final ParkingService parkingService;
    private final AdministrativeAccess administrativeAccess;

    public ParkingStayController(ParkingService parkingService, AdministrativeAccess administrativeAccess) {
        this.parkingService = parkingService;
        this.administrativeAccess = administrativeAccess;
    }

    @PostMapping("/entries")
    public ResponseEntity<ParkingStayResponse> registerEntry(
            @Valid @RequestBody RegisterEntryRequest request) {
        ParkingStayResponse response = ParkingStayResponse.from(parkingService.registerEntry(request.plate()));
        AUDIT_LOG.info("action=entry_registered stayId={} plate={}", response.id(), response.plate());
        return ResponseEntity.created(URI.create("/api/v1/stays/" + response.id())).body(response);
    }

    @PostMapping("/exits")
    public ParkingStayResponse registerExit(@Valid @RequestBody RegisterExitRequest request) {
        ParkingStayResponse response = ParkingStayResponse.from(parkingService.registerExit(request.plate()));
        AUDIT_LOG.info("action=exit_registered stayId={} plate={}", response.id(), response.plate());
        return response;
    }

    @GetMapping
    public List<ParkingStayResponse> listAll(
            @RequestParam(required = false) String plate,
            @RequestParam(required = false) StayStatus status,
            @RequestHeader(name = "X-Admin-Token", required = false) String adminToken) {
        administrativeAccess.require(adminToken);
        AUDIT_LOG.info("action=history_consulted plateFilter={} statusFilter={}", plate, status);
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

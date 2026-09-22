package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.service.PlateDetectionService;
import br.com.estacionamento.interfaces.rest.dto.ConfirmPlateDetectionRequest;
import br.com.estacionamento.interfaces.rest.dto.DismissPlateDetectionRequest;
import br.com.estacionamento.interfaces.rest.dto.PlateDetectionRequest;
import br.com.estacionamento.interfaces.rest.dto.PlateDetectionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/plate-detections")
public class PlateDetectionController {
    private static final Logger DETECTION_LOG = LoggerFactory.getLogger("AUDIT");
    private final PlateDetectionService detectionService;
    private final AdministrativeAccess administrativeAccess;
    private final Clock clock;
    public PlateDetectionController(PlateDetectionService detectionService, AdministrativeAccess administrativeAccess, Clock clock) {
        this.detectionService = detectionService; this.administrativeAccess = administrativeAccess; this.clock = clock;
    }
    @PostMapping
    public ResponseEntity<PlateDetectionResponse> receive(@Valid @RequestBody PlateDetectionRequest request) {
        PlateDetectionResponse response = PlateDetectionResponse.from(detectionService.receive(
                request.plate(), request.confidence(), request.direction(), request.detectedAt() == null ? clock.instant() : request.detectedAt()));
        DETECTION_LOG.info("action=plate_detection_received detectionId={} direction={} confidence={} status={}",
                response.id(), response.direction(), response.confidence(), response.status());
        return ResponseEntity.created(URI.create("/api/v1/plate-detections/" + response.id())).body(response);
    }
    @GetMapping("/pending")
    public List<PlateDetectionResponse> pending(@RequestHeader(name = "X-Admin-Token", required = false) String adminToken) {
        administrativeAccess.require(adminToken);
        return detectionService.listPending().stream().map(PlateDetectionResponse::from).toList();
    }
    @PostMapping("/{id}/confirm")
    public PlateDetectionResponse confirm(@PathVariable UUID id, @Valid @RequestBody ConfirmPlateDetectionRequest request,
                                          @RequestHeader(name = "X-Admin-Token", required = false) String adminToken) {
        administrativeAccess.require(adminToken);
        return PlateDetectionResponse.from(detectionService.confirm(id, request.plate()));
    }
    @PostMapping("/{id}/dismiss")
    public PlateDetectionResponse dismiss(@PathVariable UUID id, @RequestBody(required = false) DismissPlateDetectionRequest request,
                                          @RequestHeader(name = "X-Admin-Token", required = false) String adminToken) {
        administrativeAccess.require(adminToken);
        return PlateDetectionResponse.from(detectionService.dismiss(id, request == null ? null : request.reason()));
    }
}

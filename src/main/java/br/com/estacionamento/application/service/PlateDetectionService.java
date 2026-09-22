package br.com.estacionamento.application.service;

import br.com.estacionamento.application.exception.ActiveStayAlreadyExistsException;
import br.com.estacionamento.application.exception.ActiveStayNotFoundException;
import br.com.estacionamento.application.exception.DetectionNotFoundException;
import br.com.estacionamento.application.port.PlateDetectionRepository;
import br.com.estacionamento.application.port.PlateReader;
import br.com.estacionamento.domain.exception.InvalidLicensePlateException;
import br.com.estacionamento.domain.model.DetectionDirection;
import br.com.estacionamento.domain.model.DetectionStatus;
import br.com.estacionamento.domain.model.ParkingStay;
import br.com.estacionamento.domain.model.PlateDetection;
import br.com.estacionamento.domain.model.PlateRead;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Coordinates a swappable reader integration while keeping parking rules in ParkingService. */
public class PlateDetectionService implements PlateReader {
    private static final double AUTO_PROCESS_CONFIDENCE = 0.90;
    private final PlateDetectionRepository repository;
    private final ParkingService parkingService;
    private final Clock clock;

    public PlateDetectionService(PlateDetectionRepository repository, ParkingService parkingService, Clock clock) {
        this.repository = repository;
        this.parkingService = parkingService;
        this.clock = clock;
    }

    @Transactional
    public PlateDetection receive(String rawPlate, double confidence, DetectionDirection direction, Instant detectedAt) {
        return receive(new PlateRead(rawPlate, confidence, direction, detectedAt));
    }

    @Override
    @Transactional
    public PlateDetection receive(PlateRead read) {
        PlateDetection detection = repository.save(PlateDetection.receive(UUID.randomUUID(), read.rawPlate(), read.confidence(), read.direction(), read.detectedAt()));
        if (read.confidence() < AUTO_PROCESS_CONFIDENCE) {
            detection.keepPending("Confiança abaixo de 90%; confirme a placa antes de registrar a movimentação.");
            return repository.save(detection);
        }
        return attemptProcessing(detection, read.rawPlate());
    }

    @Transactional
    public PlateDetection confirm(UUID id, String plate) {
        PlateDetection detection = pending(id);
        return attemptProcessing(detection, plate);
    }

    @Transactional
    public PlateDetection dismiss(UUID id, String reason) {
        PlateDetection detection = pending(id);
        detection.dismiss(reason == null || reason.isBlank() ? "Descartada pelo atendente." : reason.trim(), clock.instant());
        return repository.save(detection);
    }

    @Transactional(readOnly = true)
    public List<PlateDetection> listPending() { return repository.findAllByStatus(DetectionStatus.PENDING); }

    private PlateDetection pending(UUID id) {
        PlateDetection detection = repository.findById(id).orElseThrow(() -> new DetectionNotFoundException(id));
        if (detection.status() != DetectionStatus.PENDING) throw new IllegalStateException("A detecção já foi tratada");
        return detection;
    }

    private PlateDetection attemptProcessing(PlateDetection detection, String rawPlate) {
        try {
            ParkingStay stay = detection.direction() == DetectionDirection.ENTRY
                    ? parkingService.registerEntry(rawPlate)
                    : parkingService.registerExit(rawPlate);
            detection.process(clock.instant());
        } catch (InvalidLicensePlateException exception) {
            detection.keepPending("Não foi possível normalizar a placa detectada. Informe a placa manualmente.");
        } catch (ActiveStayAlreadyExistsException exception) {
            detection.keepPending("Já existe uma estadia ativa para esta placa; confirme a movimentação manualmente.");
        } catch (ActiveStayNotFoundException exception) {
            detection.keepPending("Não há estadia ativa para esta placa; confirme a saída manualmente.");
        }
        return repository.save(detection);
    }
}

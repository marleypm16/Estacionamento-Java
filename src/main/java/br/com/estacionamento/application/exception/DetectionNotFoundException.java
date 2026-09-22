package br.com.estacionamento.application.exception;

import java.util.UUID;

public class DetectionNotFoundException extends RuntimeException {
    public DetectionNotFoundException(UUID id) { super("Detecção não encontrada: " + id); }
}

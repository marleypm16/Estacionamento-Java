package br.com.estacionamento.application.exception;

import java.util.UUID;

public class StayNotFoundException extends RuntimeException {

    public StayNotFoundException(UUID id) {
        super("Estadia não encontrada: " + id);
    }
}

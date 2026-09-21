package br.com.estacionamento.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterExitRequest(@NotBlank(message = "A placa é obrigatória") String plate) {
}

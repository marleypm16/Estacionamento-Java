package br.com.estacionamento.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPlateDetectionRequest(@NotBlank(message = "A placa confirmada é obrigatória") String plate) { }

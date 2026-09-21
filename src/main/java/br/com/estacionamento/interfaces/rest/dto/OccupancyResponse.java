package br.com.estacionamento.interfaces.rest.dto;

import br.com.estacionamento.application.service.Occupancy;

public record OccupancyResponse(int capacity, long occupied, long available) {

    public static OccupancyResponse from(Occupancy occupancy) {
        return new OccupancyResponse(occupancy.capacity(), occupancy.occupied(), occupancy.available());
    }
}

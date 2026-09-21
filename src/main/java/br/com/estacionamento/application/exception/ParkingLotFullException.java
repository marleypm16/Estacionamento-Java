package br.com.estacionamento.application.exception;

public class ParkingLotFullException extends RuntimeException {

    public ParkingLotFullException() {
        super("O estacionamento está lotado");
    }
}

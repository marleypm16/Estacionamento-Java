package br.com.estacionamento.domain.exception;

public class InvalidLicensePlateException extends IllegalArgumentException {

    public InvalidLicensePlateException(String message) {
        super(message);
    }
}

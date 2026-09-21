package br.com.estacionamento.application.exception;

public class ActiveStayAlreadyExistsException extends RuntimeException {

    public ActiveStayAlreadyExistsException(String plate) {
        super("Já existe uma estadia ativa para a placa " + plate);
    }
}

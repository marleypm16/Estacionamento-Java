package br.com.estacionamento.application.exception;

public class ActiveStayNotFoundException extends RuntimeException {

    public ActiveStayNotFoundException(String plate) {
        super("Não existe estadia ativa para a placa " + plate);
    }
}

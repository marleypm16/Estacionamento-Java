package br.com.estacionamento.application.exception;

public class AdministrativeAccessDeniedException extends RuntimeException {

    public AdministrativeAccessDeniedException() {
        super("Acesso administrativo necessário para consultar o histórico");
    }
}

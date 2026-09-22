package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.exception.AdministrativeAccessDeniedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AdministrativeAccess {

    private final String configuredToken;

    public AdministrativeAccess(@Value("${parking.admin-token:}") String configuredToken) {
        this.configuredToken = configuredToken;
    }

    public void require(String providedToken) {
        if (configuredToken.isBlank()) {
            return;
        }

        byte[] expected = configuredToken.getBytes(StandardCharsets.UTF_8);
        byte[] provided = (providedToken == null ? "" : providedToken).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, provided)) {
            throw new AdministrativeAccessDeniedException();
        }
    }
}

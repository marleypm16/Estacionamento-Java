package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.exception.AdministrativeAccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdministrativeAccessTest {

    @Test
    void shouldAllowHistoryInLocalDevelopmentWhenNoTokenIsConfigured() {
        assertDoesNotThrow(() -> new AdministrativeAccess("").require(null));
    }

    @Test
    void shouldRequireTheConfiguredToken() {
        AdministrativeAccess access = new AdministrativeAccess("secret");

        assertDoesNotThrow(() -> access.require("secret"));
        assertThrows(AdministrativeAccessDeniedException.class, () -> access.require("invalid"));
        assertThrows(AdministrativeAccessDeniedException.class, () -> access.require(null));
    }
}

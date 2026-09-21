package br.com.estacionamento.domain.model;

import br.com.estacionamento.domain.exception.InvalidLicensePlateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicensePlateTest {

    @Test
    void shouldNormalizeLegacyPlate() {
        assertThat(LicensePlate.of("abc-1234").value()).isEqualTo("ABC1234");
    }

    @Test
    void shouldAcceptMercosurPlate() {
        assertThat(LicensePlate.of("abc1d23").value()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldRejectBlankOrInvalidPlate() {
        assertThatThrownBy(() -> LicensePlate.of(" "))
                .isInstanceOf(InvalidLicensePlateException.class);
        assertThatThrownBy(() -> LicensePlate.of("INVALIDA"))
                .isInstanceOf(InvalidLicensePlateException.class);
    }
}

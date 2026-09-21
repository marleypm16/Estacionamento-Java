package br.com.estacionamento.infrastructure.persistence;

import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaParkingStayRepository.class)
class JpaParkingStayRepositoryTest {

    @Autowired
    private JpaParkingStayRepository repository;

    @Test
    void shouldPersistAndFindActiveStayByNormalizedPlate() {
        ParkingStay stay = ParkingStay.start(
                UUID.randomUUID(),
                LicensePlate.of("ABC1D23"),
                Instant.parse("2026-09-21T10:00:00Z"));

        repository.save(stay);

        assertThat(repository.findActiveByPlate(LicensePlate.of("abc1d23")))
                .isPresent()
                .get()
                .extracting(found -> found.licensePlate().value())
                .isEqualTo("ABC1D23");
    }
}

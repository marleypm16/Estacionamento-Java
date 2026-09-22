package br.com.estacionamento.infrastructure.config;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.domain.model.LicensePlate;
import br.com.estacionamento.domain.model.ParkingStay;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Configuration
@ConditionalOnProperty(name = "parking.demo-data", havingValue = "true")
class DemoDataConfiguration {
    @Bean
    ApplicationRunner demoData(ParkingStayRepository repository, Clock clock) {
        return args -> {
            if (!repository.findAll(null, null).isEmpty()) return;
            Instant now = clock.instant();
            repository.save(ParkingStay.start(UUID.randomUUID(), LicensePlate.of("DEM1A01"), now.minus(38, ChronoUnit.MINUTES)));
            ParkingStay completed = ParkingStay.start(UUID.randomUUID(), LicensePlate.of("DEM2B02"), now.minus(3, ChronoUnit.HOURS));
            completed.finish(now.minus(25, ChronoUnit.MINUTES), new BigDecimal("16.00"));
            repository.save(completed);
        };
    }
}

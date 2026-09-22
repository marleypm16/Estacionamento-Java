package br.com.estacionamento.infrastructure.config;

import br.com.estacionamento.application.port.ParkingStayRepository;
import br.com.estacionamento.application.port.PlateDetectionRepository;
import br.com.estacionamento.application.service.OperationalReportService;
import br.com.estacionamento.application.service.ParkingService;
import br.com.estacionamento.application.service.PlateDetectionService;
import br.com.estacionamento.domain.model.Tariff;
import br.com.estacionamento.domain.service.DefaultTariffCalculator;
import br.com.estacionamento.domain.service.TariffCalculator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(ParkingProperties.class)
public class ParkingConfiguration {

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    TariffCalculator tariffCalculator() {
        return new DefaultTariffCalculator();
    }

    @Bean
    ParkingService parkingService(
            ParkingStayRepository repository,
            TariffCalculator tariffCalculator,
            Clock clock,
            ParkingProperties properties) {
        ParkingProperties.TariffProperties tariffProperties = properties.getTariff();
        Tariff tariff = new Tariff(
                tariffProperties.getInitialFee(),
                tariffProperties.getHourlyFee(),
                tariffProperties.getGracePeriodMinutes());
        return new ParkingService(
                repository,
                tariffCalculator,
                tariff,
                clock,
                properties.getCapacity());
    }

    @Bean
    OperationalReportService operationalReportService(ParkingStayRepository repository) {
        return new OperationalReportService(repository);
    }

    @Bean
    PlateDetectionService plateDetectionService(PlateDetectionRepository repository, ParkingService parkingService, Clock clock) {
        return new PlateDetectionService(repository, parkingService, clock);
    }
}

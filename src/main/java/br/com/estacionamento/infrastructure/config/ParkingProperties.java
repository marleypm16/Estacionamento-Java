package br.com.estacionamento.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "parking")
public class ParkingProperties {

    private int capacity = 50;
    private final TariffProperties tariff = new TariffProperties();

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public TariffProperties getTariff() {
        return tariff;
    }

    public static class TariffProperties {
        private BigDecimal initialFee = new BigDecimal("8.00");
        private BigDecimal hourlyFee = new BigDecimal("4.00");
        private long gracePeriodMinutes;

        public BigDecimal getInitialFee() {
            return initialFee;
        }

        public void setInitialFee(BigDecimal initialFee) {
            this.initialFee = initialFee;
        }

        public BigDecimal getHourlyFee() {
            return hourlyFee;
        }

        public void setHourlyFee(BigDecimal hourlyFee) {
            this.hourlyFee = hourlyFee;
        }

        public long getGracePeriodMinutes() {
            return gracePeriodMinutes;
        }

        public void setGracePeriodMinutes(long gracePeriodMinutes) {
            this.gracePeriodMinutes = gracePeriodMinutes;
        }
    }
}

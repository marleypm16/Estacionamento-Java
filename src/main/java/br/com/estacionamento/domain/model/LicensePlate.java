package br.com.estacionamento.domain.model;

import br.com.estacionamento.domain.exception.InvalidLicensePlateException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record LicensePlate(String value) {

    private static final Pattern BRAZILIAN_PLATE =
            Pattern.compile("(?:[A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})");

    public LicensePlate {
        Objects.requireNonNull(value, "A placa é obrigatória");
        if (!BRAZILIAN_PLATE.matcher(value).matches()) {
            throw new InvalidLicensePlateException("Placa inválida: use o formato ABC1234 ou ABC1D23");
        }
    }

    public static LicensePlate of(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new InvalidLicensePlateException("A placa é obrigatória");
        }

        String normalized = rawValue
                .replace("-", "")
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
        return new LicensePlate(normalized);
    }

    @Override
    public String toString() {
        return value;
    }
}

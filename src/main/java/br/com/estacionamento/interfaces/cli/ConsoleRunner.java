package br.com.estacionamento.interfaces.cli;

import br.com.estacionamento.application.service.ParkingService;
import br.com.estacionamento.domain.model.ParkingStay;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class ConsoleRunner implements CommandLineRunner {

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final ParkingService parkingService;

    public ConsoleRunner(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String option = scanner.nextLine().trim();
                try {
                    running = handleOption(option, scanner);
                } catch (RuntimeException exception) {
                    System.out.println("Erro: " + exception.getMessage());
                }
            }
        }
        System.out.println("Aplicação encerrada.");
    }

    private boolean handleOption(String option, Scanner scanner) {
        return switch (option) {
            case "1" -> {
                String plate = readPlate(scanner);
                ParkingStay stay = parkingService.registerEntry(plate);
                System.out.printf("Entrada registrada para %s em %s.%n",
                        stay.licensePlate(), DATE_TIME.format(stay.enteredAt()));
                yield true;
            }
            case "2" -> {
                listActive();
                yield true;
            }
            case "3" -> {
                String plate = readPlate(scanner);
                ParkingStay stay = parkingService.registerExit(plate);
                System.out.printf("Saída registrada para %s. Valor: %s.%n",
                        stay.licensePlate(), formatCurrency(stay.amountCharged()));
                yield true;
            }
            case "4" -> false;
            default -> {
                System.out.println("Opção inválida. Escolha entre 1 e 4.");
                yield true;
            }
        };
    }

    private void listActive() {
        List<ParkingStay> stays = parkingService.listActive();
        if (stays.isEmpty()) {
            System.out.println("Não há veículos estacionados.");
            return;
        }
        System.out.println("Veículos estacionados:");
        stays.forEach(stay -> System.out.printf("- %s | entrada: %s%n",
                stay.licensePlate(), DATE_TIME.format(stay.enteredAt())));
    }

    private String readPlate(Scanner scanner) {
        System.out.print("Digite a placa: ");
        return scanner.nextLine();
    }

    private String formatCurrency(BigDecimal value) {
        return CURRENCY.format(value);
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Estacionamento ===");
        System.out.println("1 - Registrar entrada");
        System.out.println("2 - Listar veículos");
        System.out.println("3 - Registrar saída");
        System.out.println("4 - Sair");
        System.out.print("Opção: ");
    }
}

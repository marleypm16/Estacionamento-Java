import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Estacionamento {
    private BigDecimal precoInicial;
    private BigDecimal precoFinal;
    private List<String> veiculos;
    private Scanner sc = new Scanner(System.in);

    public Estacionamento(BigDecimal precoInicial) {
        this.precoInicial = precoInicial;
        this.veiculos = new ArrayList<String>();
    }
    // adiciona a placa do carro a uma lista
    public void adicionaVeiculo() {
        System.out.println("Digite a placa do veiculo: ");
        String veiculo = sc.nextLine();
        this.veiculos.add(veiculo);
        System.out.println("Veiculo adicionado com sucesso!");
    }
    //lista todos os veiculos
    public void listarVeiculos() {
        System.out.println("Véiculos cadastrados:");
        if (this.veiculos.isEmpty()) {
            System.out.println("Não há veiculos cadastrados.");
        }
        for (String veiculo : this.veiculos) {
            System.out.println(veiculo);
        }
    }
    // remove o veiculo da lista e retorna o valor a ser pago
    public void removerVeiculo() {
        System.out.println("Digite a placa do veiculo: ");
        String veiculo = sc.nextLine();
        System.out.println("Digite o tempo que ele ficou estacionado: ");
        String tempo = sc.nextLine();
        int tempoInt= Integer.parseInt(tempo);
        this.veiculos.remove(veiculo);
        precoFinal = precoInicial.multiply(new BigDecimal(tempoInt));
        System.out.println("Veiculo removido com sucesso! Valor a ser pago ${precoFinal}");
    }
}

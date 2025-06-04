import java.math.BigDecimal;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Estacionamento estacionamento = new Estacionamento(new BigDecimal(8));
        Scanner sc = new Scanner(System.in);
        System.out.println("Bem vindo ao estacionamento! O que deseja fazer?");

        while (true){
            System.out.println("1- Cadastrar Veiculo");
            System.out.println("2- Listar Veiculos");
            System.out.println("3- Excluir Veiculos");
            System.out.println("4- Sair");
            int opcao = sc.nextInt();
            switch (opcao){
                case 1:
                    estacionamento.adicionaVeiculo();
                    break;

                case 2:
                    estacionamento.listarVeiculos();
                    System.out.println("Aperte qualquer tecla para continuar");
                    sc.nextLine();
                    break;
                case 3:
                    estacionamento.removerVeiculo();
                    System.out.println("Aperte qualquer tecla para continuar");
                    sc.nextLine();
                    break;
                case 4:
                    System.exit(0);
                    break;
            }
        }
    }
}
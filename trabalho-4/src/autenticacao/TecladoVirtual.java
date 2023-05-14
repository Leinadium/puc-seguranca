package autenticacao;
import criptografia.CriptoSenha;
import terminal.InterfaceTerminal;

import java.util.*;

public class TecladoVirtual {
    final ArrayList<Integer> botoes;
    final ArrayList<ArrayList<Integer>> senhaInserida = new ArrayList<>();

    public TecladoVirtual() {
        // configura os botoes do teclado
        this.botoes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.botoes.add(i);
        }
    }

    private String comparaSenhas(String senhaCorreta, String[] todasSenhas) {
        for (String senha : todasSenhas) {
            if (CriptoSenha.compara(senhaCorreta, senha)) {
                return senha;
            }
        }
        return null;
    }

    public String lerSenha(String senhaCorreta, int tentativasRestantes) {
        Scanner scanner = new Scanner(System.in);

        for (int i = 0; i < 10; i += 1) {
            Collections.shuffle(this.botoes);
            boolean enviou = escolherBotao(scanner, this.botoes,i >= 8, i, tentativasRestantes);
            if (enviou){
                break;
            }
        }
        String[] todasSenhas = gerarTodasSenhas(senhaInserida);

        System.out.println("Carregando...");

        return comparaSenhas(senhaCorreta, todasSenhas);
    }

    private boolean escolherBotao(Scanner scanner, ArrayList<Integer> botoes, boolean podeEnviar, int digitados, int tentativasRestantes) {
        InterfaceTerminal.limparTela();
        System.out.println("===========================");
        // builda os asteriscos
        StringBuilder asteriscos = new StringBuilder();
        for (int i = 0; i < digitados; i++) {
            asteriscos.append("*");
        }
        // https://stackoverflow.com/questions/2255500/can-i-multiply-strings-in-java-to-repeat-sequences
        System.out.println("Digite a sua senha: " + asteriscos);
        if (tentativasRestantes > 0) {
            System.out.println("Tentativas restantes: " + (3 - tentativasRestantes));
        }


        for (int i = 0; i < 5; i++) {
            System.out.printf("%d: [%d,%d]\n", i + 1, botoes.get(i * 2), botoes.get(i * 2 + 1));
        }
        int opcao;
        do {
            System.out.print("\nDigite o número do botão: ");
            String opcaoString = scanner.nextLine();
            if (opcaoString.equals("")) {
                if (podeEnviar) {
                    return true;
                }
                opcao = -1;
            }
            else {
                try {
                    opcao = Integer.parseInt(opcaoString);
                } catch (NumberFormatException e) {
                    opcao = -1;
                }
            }
        } while (opcao < 1 || opcao > 5);
        int botaoSelecionado = opcao - 1;
        int valor1 = botoes.get(botaoSelecionado * 2);
        int valor2 = botoes.get(botaoSelecionado * 2 + 1);

        this.senhaInserida.add(new ArrayList<>(Arrays.asList(valor1, valor2)));
        return false;
    }

    private String[] gerarTodasSenhas(ArrayList<ArrayList<Integer>> senhaInserida) {
        // Crie um array de strings para armazenar todas as combinações de senha
        int numCombinacoes = (int) Math.pow(2, senhaInserida.size());
        String[] todasSenhas = new String[numCombinacoes];

        // Gerar todas as combinações de senhas
        for (int i = 0; i < numCombinacoes; i++) {
            StringBuilder senha = new StringBuilder();
            for (int j = 0; j < senhaInserida.size(); j++) {
                if ((i & (1 << j)) != 0) {
                    senha.append(senhaInserida.get(j).get(0));
                } else {
                    senha.append(senhaInserida.get(j).get(1));
                }
            }
            todasSenhas[i] = senha.toString();
        }

        return todasSenhas;
    }
}

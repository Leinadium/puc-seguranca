package autenticacao;
import java.util.*;

public class TecladoVirtual {

    public static void main(String[] args) {
        TecladoVirtual teclado = new TecladoVirtual();
        String senhaCorretaPlainText = "1234567890";
        byte[] saltQualquer = Base64.getEncoder().encode("TRQ1SYrgQd".getBytes());
        String senhaCorretaBCrypt = CriptoSenha.encripta(senhaCorretaPlainText, saltQualquer);
        teclado.lerSenha(senhaCorretaBCrypt);
    }

    private final ArrayList<Integer> botoes;
    private final ArrayList<ArrayList<Integer>> senhaInserida = new ArrayList<>();

    public TecladoVirtual() {
        this.botoes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.botoes.add(i);
        }
    }

    private boolean comparaDigito(int digitoInserido, int digitoCorreto) {
        return digitoInserido == digitoCorreto;
    }

    private boolean comparaSenhas(String senhaCorreta, String[] todasSenhasHash) {
        for (String senhaHash : todasSenhasHash) {
            if (CriptoSenha.compara(senhaCorreta, senhaHash)) {
                return true;
            }
        }
        return false;
    }

    public boolean lerSenha(String senhaCorreta) {
        Scanner scanner = new Scanner(System.in);


        for (int i = 0; i < 10; i += 1) {
            Collections.shuffle(this.botoes);
            boolean enviou = escolherBotao(scanner, this.botoes,i >= 8);
            if (enviou){
                break;
            }
        }
        String[] todasSenhas = gerarTodasSenhas(senhaInserida);

        boolean senhasIguais = comparaSenhas(senhaCorreta, todasSenhas);

        if(senhasIguais){
            System.out.println("Senha correta!");
        }
        else{
            System.out.println("Senha incorreta!");
        }

        return senhasIguais;
    }

    private boolean escolherBotao(Scanner scanner, ArrayList<Integer> botoes, boolean podeEnviar) {
        for (int i = 0; i < 5; i++) {
            System.out.printf("%d: [%d,%d]%n", i + 1, botoes.get(i * 2), botoes.get(i * 2 + 1));
        }
        int opcao;
        do {
            System.out.print("Digite o número do botão: ");
            String opcaoString = scanner.nextLine();
            if (opcaoString.equals("")) {
                if (podeEnviar) {
                    return true;
                }
                opcao = -1;
            }
            else {
                opcao = Integer.parseInt(opcaoString);
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

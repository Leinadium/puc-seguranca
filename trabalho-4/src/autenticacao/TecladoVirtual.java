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

    private static Scanner scanner = new Scanner(System.in);
    private ArrayList<Integer> botoes = new ArrayList<Integer>();
    private ArrayList<ArrayList<Integer>> senhaInserida = new ArrayList<>();

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

    public String lerSenha(String senhaCorreta) {
        Scanner scanner = new Scanner(System.in);
        List<Integer> numeros = new ArrayList<>();
        for (int i = 0; i < 10; i += 2) {
            Collections.shuffle(this.botoes);
            int opcao = escolherBotao(scanner, this.botoes);
            numeros.add(this.botoes.get(opcao));
            Collections.shuffle(this.botoes);
            opcao = escolherBotao(scanner, this.botoes);
            numeros.add(this.botoes.get(opcao));
        }
        String[] todasSenhas = gerarTodasSenhas(senhaInserida);
        System.out.println("Todas as senhas: " + Arrays.toString(todasSenhas));

        // String[] todasSenhasHash = hashTodasSenhas(todasSenhas);
        // System.out.println("Todas as senhas hash: " + Arrays.toString(todasSenhasHash));

        System.out.println("SALVE");
        boolean senhasIguais = comparaSenhas(senhaCorreta, todasSenhas);
        if (senhasIguais) {
            System.out.println("Senha correta!");
        } else {
            System.out.println("Senha incorreta!");
        }
        return senhaCorreta;
    }

    private int escolherBotao(Scanner scanner, ArrayList<Integer> botoes) {
        for (int i = 0; i < 5; i++) {
            System.out.printf("%d: [%d,%d]%n", i + 1, botoes.get(i * 2), botoes.get(i * 2 + 1));
        }
        int opcao;
        do {
            System.out.print("Digite o número do botão: ");
            opcao = scanner.nextInt();
        } while (opcao < 1 || opcao > 5);
        int botaoSelecionado = opcao - 1;
        int valor1 = botoes.get(botaoSelecionado * 2);
        int valor2 = botoes.get(botaoSelecionado * 2 + 1);

        this.senhaInserida.add(new ArrayList<>(Arrays.asList(valor1, valor2)));
        return opcao - 1;
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
        System.out.println("Numero de senhas geradas: " + numCombinacoes);
        System.out.println("Todas as senhas: " + Arrays.toString(todasSenhas));

        return todasSenhas;
    }

    private static String toHex(byte[] meusBytes) {
        // converte o signature para hexadecimal
        StringBuilder buf = new StringBuilder();
        for (byte meusByte : meusBytes) {
            String hex = Integer.toHexString(0x0100 + (meusByte & 0x00FF)).substring(1);
            buf.append(hex.length() < 2 ? "0" : "").append(hex);
        }
        return buf.toString();
    }
}

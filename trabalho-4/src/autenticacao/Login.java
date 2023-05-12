package autenticacao;

import basedados.Conexao;
import basedados.modelos.Usuario;

import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

public class Login {

    // Regex para verificar se o login name é um endereço de e-mail válido
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static void main(String[] args) {
        Conexao conexao;
        try {
            conexao = Conexao.getInstance();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            System.exit(1);
            return;
        }

        while (true) {
            String nomeLogin = getUserInput("Por favor, digite o seu login name:");
            System.out.print("Login name inserido: " + nomeLogin + "\n");

            if (!emailValido(nomeLogin)) {
                System.out.println("O login name inserido não está no padrão de um e-mail.");
            } else { // Verifica se o login name inserido é um e-mail válido no banco de dados.
                Usuario usuario;
                try {
                    usuario = conexao.getUsuario(nomeLogin);
                } catch (Exception e) {
                    usuario = null;
                }

                if (usuario != null) {

                    // TODO
                    // this.conexao.usuarioEstaBloqueado(usuario)  -> retorna true se estiver bloqueado
                    // this.conexao.bloquearUsuario(usuario)    -> bloqueia o usuario

                    if (bloqueado(nomeLogin)) {
                        System.out.println("O acesso do usuário está bloqueado.");
                    } else {
                        System.out.println("O login name inserido é um e-mail válido!");
                        boolean senhaCheck;
                        int tentativas = 0;
                        String senhaCorretaPlainText = "1234567890";
                        byte[] saltQualquer = Base64.getEncoder().encode("TRQ1SYrgQd".getBytes());
                        String senhaCorretaBCrypt = CriptoSenha.encripta(senhaCorretaPlainText, saltQualquer);

                        while (true) {
                            if (tentativas == 3) {
                                System.out.println("O acesso do usuário foi bloqueado.");
                                break;
                            } else {
                                TecladoVirtual teclado = new TecladoVirtual();
                                senhaCheck = teclado.lerSenha(senhaCorretaBCrypt);
                                if (senhaCheck) {
                                    System.out.println("Senha correta!");
                                    tentativas = 0;
                                    return;
                                } else {
                                    System.out.println("Senha incorreta!");
                                    tentativas++;
                                    System.out.println("Tentativas restantes: " + (3 - tentativas));
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("O login name inserido não é um e-mail válido.");
                }
            }
        }
    }

    private static String getUserInput(String prompt) {

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println(prompt);
        return scanner.nextLine();
    }

    // Função para verificar se um login name corresponde a um endereço de e-mail válido
    private static boolean emailValido(String nomeLogin) {
        return EMAIL_PATTERN.matcher(nomeLogin).matches();
    }

    // Função para verificar se o acesso de um usuário está bloqueado
    private static boolean bloqueado(String nomeLogin) {

        String[] usuariosBloqueados = { "usuario1@exemplo.com", "usuario2@exemplo.com" };
        for (String blockedUser : usuariosBloqueados) {
            if (blockedUser.equals(nomeLogin)) {
                return true;
            }
        }
        return false;
    }
}

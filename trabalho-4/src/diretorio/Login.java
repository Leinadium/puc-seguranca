package diretorio;

import java.util.Objects;
import java.util.regex.Pattern;

public class Login {

    // Regex para verificar se o login name é um endereço de e-mail válido
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static void main(String[] args) {
        while (true) {
            String loginName = getUserInput("Por favor, digite o seu login name:");
            System.out.print("Login name inserido: " + loginName + "\n");

            if (!isValidEmail(loginName)) {
                System.out.println("O login name inserido não está no padrão de um e-mail.");
            } else { // Verifica se o login name inserido é um e-mail válido no banco de dados.
                if (Objects.equals(loginName, "teste@teste.com")) {
                    if (isBlocked(loginName)) {
                        System.out.println("O acesso do usuário está bloqueado.");
                    } else {
                        System.out.println("O login name inserido é um e-mail válido!");
                        return;
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
    private static boolean isValidEmail(String loginName) {
        return EMAIL_PATTERN.matcher(loginName).matches();
    }

    // Função para verificar se o acesso de um usuário está bloqueado
    private static boolean isBlocked(String loginName) {

        String[] blockedUsers = { "usuario1@exemplo.com", "usuario2@exemplo.com" };
        for (String blockedUser : blockedUsers) {
            if (blockedUser.equals(loginName)) {
                return true;
            }
        }
        return false;
    }
}

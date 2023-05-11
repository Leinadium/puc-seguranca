package autenticacao;

import java.util.Scanner;
import java.util.Date;
public class TokenVerifier {

    private static final String SECRET_KEY = "1234";
    public static void main(String[] args) {
        long currentTime = new Date().getTime()/ (60000);
        System.out.print("Token: " + TokenGenerator.generateToken(SECRET_KEY, currentTime) + "\n");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o token do usuário: ");
        int userToken = scanner.nextInt();
        scanner.close();

        boolean isValid = verifyToken(SECRET_KEY, userToken);

        if (isValid) {
            System.out.println("Token válido.");
        } else {
            System.out.println("Token inválido.");
        }
    }

    private static boolean verifyToken(String PRIVATE_KEY, int userToken) {
        long currentTime = new Date().getTime();
        long currentMinute = currentTime / 60000;
        long previousMinute = currentMinute - 1;
        long nextMinute = currentMinute + 1;
        int[] expectedTokens = { TokenGenerator.generateToken(SECRET_KEY,previousMinute), TokenGenerator.generateToken(SECRET_KEY,currentMinute), TokenGenerator.generateToken(SECRET_KEY,nextMinute) };
        System.out.print("Token esperado: " + expectedTokens[0] + "\n Token esperado: " + expectedTokens[1] + "\n Token esperado: " + expectedTokens[2] + "\n");
        for (int expectedToken : expectedTokens) {
            if (expectedToken == userToken) {
                return true;
            }
        }
        return false;
    }

}
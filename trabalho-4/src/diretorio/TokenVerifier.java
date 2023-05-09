package diretorio;

import diretorio.TokenGenerator;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
public class TokenVerifier {
    public static void main(String[] args) {
        long currentTime = new Date().getTime()/ (60000);
        System.out.print("Token: " + TokenGenerator.generateToken(currentTime) + "\n");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o token do usuário: ");
        int userToken = scanner.nextInt();
        scanner.close();

        boolean isValid = verifyToken(userToken);

        if (isValid) {
            System.out.println("Token válido.");
        } else {
            System.out.println("Token inválido.");
        }
    }

    private static boolean verifyToken(int userToken) {
        long currentTime = new Date().getTime();
        long currentMinute = currentTime / 60000;
        long previousMinute = currentMinute - 1;
        long nextMinute = currentMinute + 1;
        int[] expectedTokens = { TokenGenerator.generateToken(previousMinute), TokenGenerator.generateToken(currentMinute), TokenGenerator.generateToken(nextMinute) };
        System.out.print("Token esperado: " + expectedTokens[0] + "\n Token esperado: " + expectedTokens[1] + "\n Token esperado: " + expectedTokens[2] + "\n");
        for (int expectedToken : expectedTokens) {
            if (expectedToken == userToken) {
                return true;
            }
        }
        return false;
    }

}
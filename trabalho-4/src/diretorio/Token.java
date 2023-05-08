package diretorio;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Token {

    private static final String SECRET_KEY = "chave_secreta_aqui"; // Semente secreta de 16 bytes

    public static int generateToken() {
        long timestamp = new Date().getTime(); // Carimbo de tempo atual em milissegundos
        String input = SECRET_KEY + timestamp; // Concatenação da semente secreta com o carimbo de tempo
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] hashedBytes = sha1.digest(input.getBytes());
            int generatedToken = Math.abs(bytesToInt(hashedBytes)) % 1000000; // Valor inteiro positivo de 6 dígitos
            return generatedToken;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean verifyToken(int userToken) {
        long timestamp = new Date().getTime(); // Carimbo de tempo atual em milissegundos
        String input = SECRET_KEY + timestamp; // Concatenação da semente secreta com o carimbo de tempo
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] hashedBytes = sha1.digest(input.getBytes());
            int generatedToken = Math.abs(bytesToInt(hashedBytes)) % 1000000; // Valor inteiro positivo de 6 dígitos
            return (userToken == generatedToken); // Verifica se o token do usuário é igual ao token gerado
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value += (bytes[i] & 0xff) << (8 * (3 - i));
        }
        return value;
    }
}
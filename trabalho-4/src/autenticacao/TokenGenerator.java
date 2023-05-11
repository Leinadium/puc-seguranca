package autenticacao;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class TokenGenerator {

    public static int generateToken(String SECRET_KEY, long currentTime) {// Carimbo de tempo atual em minutos
        String input = SECRET_KEY + currentTime; // Concatenação da semente secreta com o carimbo de tempo
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

    private static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value += (bytes[i] & 0xff) << (8 * (3 - i));
        }
        return value;
    }
}
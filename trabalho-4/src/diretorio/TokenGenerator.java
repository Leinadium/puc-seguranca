package diretorio;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class TokenGenerator {

    private static final String SECRET_KEY = "chave_secreta_aqui"; // Semente secreta de 16 bytes

    public static int generateToken(long currentTime) {// Carimbo de tempo atual em minutos
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
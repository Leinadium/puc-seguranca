package autenticacao;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GeradorToken {

    public static int geraToken(byte[] semente, long tempoAtual) {// Carimbo de tempo atual em minutos
        byte[] input = concatBytes(semente, longToBytes(tempoAtual)); // Concatenação da semente secreta com o carimbo de tempo
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] hashedBytes = sha1.digest(input);
            return Math.abs(bytesToInt(hashedBytes)) % 1000000; // Valor inteiro positivo de 6 dígitos
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

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private static byte[] concatBytes(byte[] a, byte[] b) {
        byte[] ret = new byte[a.length + b.length];
        System.arraycopy(a, 0, ret, 0, a.length);
        System.arraycopy(b, 0, ret, a.length, b.length);
        return ret;
    }
}
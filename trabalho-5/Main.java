import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception{
        // String chave = args[0];
        // String parcial = args[1];
        // String criptograma = args[2];

        String chave = "SKYWALKER2019";
        String parcial = "Star Wars Episode";      // String parcial = "Star Wars: Episode";
        String criptograma = "25d01feae4e4967162cb72a8940aac94970c8389ea7bed653258faa2228529f796293b38b3176cb6ec116b5b3582414e6025d7fb88b94e409c502ed180bd137acf03d04a235a89f918cfe18eabe75877b5e630bd35c13636a145444bb55bd1529bf52e5a4cd2ac35e9fdeeee306d4e41";

        byte[] criptogramaByte = hexStringToByteArray(criptograma);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(chave.getBytes());

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128, random);
        SecretKey simKey = kg.generateKey();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        long i;
        byte[] iv;
        byte[] res;
        String ivStr;

        long inicio = 0L;
        long fim = 9999999999999999L;

        // change default charset to ascii of toString


        for (i = inicio; i <= fim; i += 101617) {       // o padrão estava repetindo a cada 101617
            ivStr = String.format("%016d", i);
            iv = ivStr.getBytes(StandardCharsets.US_ASCII);

            cipher.init(Cipher.DECRYPT_MODE, simKey, new IvParameterSpec(iv));
            res = cipher.doFinal(criptogramaByte);

            String resStr;
            try {
                resStr = new String(res, StandardCharsets.US_ASCII);

                if (resStr.contains(parcial)) {
                    System.out.println("res: " + toHex(res));
                    System.out.println("res(str): " + resStr);
                    System.out.println("ivStr: " + ivStr);
                    // break;
                }
            } catch (Exception ignored){ }



            if (i % 99999999L == 0) {
                System.out.println("i: " + i + " ivStr: " + ivStr);
            }
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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

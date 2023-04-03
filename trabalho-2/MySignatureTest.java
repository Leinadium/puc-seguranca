/*
 *  Daniel Guimarães - 1910462
 *  Luiz Fellipe Augusto - 1711256
 */

import java.nio.charset.StandardCharsets;
import java.security.*;


public class MySignatureTest {

    /** Converte um array de butes para String */
    private static String toHex(byte[] meusBytes) {
        // converte o signature para hexadecimal
        StringBuilder buf = new StringBuilder();
        for (byte meusByte : meusBytes) {
            String hex = Integer.toHexString(0x0100 + (meusByte & 0x00FF)).substring(1);
            buf.append(hex.length() < 2 ? "0" : "").append(hex);
        }
        return buf.toString();
    }

    public static void main (String[] args) throws Exception {
        if (args.length != 2 ) {
            System.err.println("Uso: java MySignatureTest padraoAssinatura documento");
            System.exit(1);
        }
        // argumentos
        String padraoAssinatura = args[0];
        String[] algoritmos = padraoAssinatura.split("with");
        byte[] documento = args[0].getBytes(StandardCharsets.UTF_8);

        // gerando as chaves
        // poderia usar RSA direto pois todas as opcoes do MySignature utilizando RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algoritmos[1]);
        keyGen.initialize(2048);

        System.out.println("Gerando par de chaves...");
        KeyPair chaves = keyGen.generateKeyPair();
        System.out.println("\nChave publica: " + toHex(chaves.getPublic().getEncoded()));
        System.out.println("\nChave privada: " + toHex(chaves.getPrivate().getEncoded()));

        // criando o objeto MySignature e configurando
        System.out.println("\n\nConfigurando MySignature para geração...");
        MySignature sig = MySignature.getInstance(padraoAssinatura);
        sig.initSign(chaves.getPrivate());
        sig.update(documento);

        // pegando a assinatura e o digest
        byte[] signature = sig.sign();
        MessageDigest md = MessageDigest.getInstance(algoritmos[0]);
        System.out.println("\n\nDigest: " + toHex(md.digest(documento)));
        System.out.println("\nAssinatura: " + toHex(signature));

        // verificando a assinatura
        System.out.println("\n\nConfigurando MySignature para verificação...");
        sig.initVerify(chaves.getPublic());
        sig.update(documento);

        System.out.println("Validando assinatura: ");
        if (sig.verify(signature)) {
            System.out.println("Assinatura válida! :)");
        } else {
            System.out.println("Assinatura inválida :(");
        }
    }
}

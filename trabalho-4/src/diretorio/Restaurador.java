package diretorio;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class Restaurador {

    /** Restaura a chave privada a partir do arquivo .key e de uma frase secreta
     * Utiliza a frase secreta num algoritmo de SHA1-PRNG para gerar uma chave simétrica do arquivo .key
     * Utiliza a chave simétrica para decriptar o arquivo .key, que resulta na chave privada em Base64
     * Depois, decodifica a chave privada em Base64 em bytes, para depois passar pelo EncodedKeySpec e KeyFactory
     * e retornar uma chave privada*/
    public static PrivateKey restauraChavePrivada(byte[] key, String senha) throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        random.setSeed(senha.getBytes());
        kg.init(56, random);
        SecretKey simKey = kg.generateKey();

        // decodifica o arquivo pathKey com a chave simetrica
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, simKey);
        byte[] keyPrivadaBytes = cipher.doFinal(key);

        // removendo do formato PEM (https://stackoverflow.com/a/63044908)
        String keyPrivadaPem = new String(keyPrivadaBytes);
        String keyPrivadaB64 = keyPrivadaPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "");

        byte[] keyPrivadaBytesDecoded = Base64.getDecoder().decode(keyPrivadaB64);

        // cria a chave privada
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyPrivadaBytesDecoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static byte[] restauraChavePrivadaBytes(String pathKey) throws Exception {
        return Files.readAllBytes(Paths.get(pathKey));
    }

    /** Restaura a chave pública a partir do arquivo
     * contendo o certificado digital da chave pública
     */
    public static Certificate restauraCertificado(String pathCert) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(Files.newInputStream(Paths.get(pathCert)));
    }

    public static String geraChavePublicaPem(PublicKey chave) throws Exception {
        PemObject pemObject = new PemObject("RSA PUBLIC KEY", chave.getEncoded());
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        return stringWriter.toString();
    }

    public static PublicKey getChavePublica(String pem) throws Exception {
        String key64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "");

        byte[] keyDecoded = Base64.getDecoder().decode(key64);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(keyDecoded));
    }
}

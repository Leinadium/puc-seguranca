package diretorio;

import registro.Registrador;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Diretorio {
    Registrador logger;
    String path;

    public Diretorio(String path) {
        this.path = path;
        this.logger = Registrador.getInstance();
    }

    /** Recebe o path para a chave privada do usuario, e começa o processo de decriptar o indice.
     * Decripta o arquivo index.env definido com a chave privada, que resulta numa semente.
     * Utiliza a semente para o algoritmo de SHA1-PRNG, gerando uma chave simétrica
     * Utiliza a chave simétrica para decriptar o arquivo index.enc, que resulta no texto plano
     */
    public String decriptaArquivo(Path pathEnv, Path pathEnc, Path pathAsd, PrivateKey pk) throws Exception{
        // decripta o arquivo index.env com a chave privada
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, pk);
        byte[] semente = cipher.doFinal(Files.readAllBytes(pathEnv));

        // gera a chave simetrica
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(semente);
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        kg.init(56, random);
        SecretKey simKey = kg.generateKey();

        // decripta o arquivo index.enc com a chave simetrica
        cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, simKey);
        byte[] textoPlano = cipher.doFinal(Files.readAllBytes(pathEnc));
        return new String(textoPlano);
    }

    /** Restaura a chave privada a partir do arquivo .key e de uma frase secreta
     * Utiliza a frase secreta num algoritmo de SHA1-PRNG para gerar uma chave simétrica do arquivo .key
     * Utiliza a chave simétrica para decriptar o arquivo .key, que resulta na chave privada em Base64
     * Depois, decodifica a chave privada em Base64 em bytes, para depois passar pelo EncodedKeySpec e KeyFactory
     * e retornar uma chave privada
     */

    public PrivateKey restauraChavePrivada(Path pathKey, String senha) throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        random.setSeed(senha.getBytes());
        kg.init(56, random);
        SecretKey simKey = kg.generateKey();

        // decodifica o arquivo pathKey com a chave simetrica
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, simKey);
        byte[] keyPrivadaBytes = cipher.doFinal(Files.readAllBytes(pathKey));

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
}

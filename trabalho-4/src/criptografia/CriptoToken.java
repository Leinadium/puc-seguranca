package criptografia;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class CriptoToken {
    public static byte[] geraSemente(String senha) {
        // gera uma senha aleatorio
        SecureRandom random = new SecureRandom();
        byte[] semente = new byte[16];
        random.nextBytes(semente);

        // encripta
        // inicia um prng usando a senha
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(senha.getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("DES");
            // gera a chave des usando a senha
            kg.init(56, random);
            SecretKey simKey = kg.generateKey();
            // encripta a semente usando a chave des
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, simKey);
            return cipher.doFinal(semente);
        } catch (Exception e) {
            System.out.println("ERRO GERANDO SEMENTE: ");
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decriptaSemente(String senha, byte[] sementeEnc) {
        // inicia um prng usando a senha
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(senha.getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("DES");
            // gera a chave des usando a senha
            kg.init(56, random);
            SecretKey simKey = kg.generateKey();
            // decripta a semente usando a chave des
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, simKey);
            return cipher.doFinal(sementeEnc);
        } catch (Exception e) {
            System.out.println("ERRO RECUPERANDO SEMENTE: ");
            e.printStackTrace();
            return null;
        }
    }
}

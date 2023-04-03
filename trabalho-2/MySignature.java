/*
 *  Daniel Guimarães - 1910462
 *  Luiz Fellipe Augusto - 1711256
 */

import java.security.*;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.*;

public class MySignature {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private MessageDigest messageDigest;
    private Cipher cipher;
    private byte[] document;

    /**
     * Cria uma instancia do MySignature configurado com os argumentos
     * @param args o algoritmo de hash e de chave
     * @return a instancia
     */
    public static MySignature getInstance(String args) throws Exception {
        String[] argsList = args.split("with");
        if (argsList.length != 2) {
            System.out.println(argsList[0]);
            System.out.println(argsList[1]);
            throw new Exception("Invalid arg");
        }
        MySignature instance = new MySignature();

        // salvando os algs
        String hashAlg = argsList[0];
        String keyAlg = argsList[1];

        if (!Objects.equals(hashAlg, "MD5") &&
                !Objects.equals(hashAlg, "SHA1") &&
                !Objects.equals(hashAlg, "SHA256") &&
                !Objects.equals(hashAlg, "SHA512")) {
            throw new Exception("algoritmo de hash invalido");
        }

        if (!Objects.equals(keyAlg, "RSA")) {
            throw new Exception("algoritmo de criptografia de chave invalido");
        }

        String transformation = keyAlg + "/ECB/PKCS1Padding";

        // criando os objetos
        instance.messageDigest = MessageDigest.getInstance(hashAlg);
        instance.cipher = Cipher.getInstance(transformation);

        return instance;
    }

    /**
     * Initializa a assinatura com a chave privada
     * @param pk chave privada
     */
    public void initSign(PrivateKey pk) {
        this.privateKey = pk;
    }

    public void update(byte[] document) {
        this.document = document;
    }

    public byte[] sign() throws Exception {
        // faz o hash
        byte[] digest = this.messageDigest.digest(this.document);

        // passando pela chave privada
        this.cipher.init(Cipher.ENCRYPT_MODE, this.privateKey);
        byte[] cifrado = this.cipher.doFinal(digest);

        // remove o documento (de acordo com a descricao do metodo original do sign)
        this.document = null;
        return cifrado;
    }

    public void initVerify(PublicKey pk) {
        this.publicKey = pk;
    }

    public boolean verify(byte[] assinatura) throws Exception {
        // fiz o hash
        byte[] digest = this.messageDigest.digest(this.document);

        // decriptar usando a public key!
        this.cipher.init(Cipher.DECRYPT_MODE, this.publicKey);
        byte[] decriptado = this.cipher.doFinal(assinatura);

        return Arrays.equals(decriptado, digest);
    }

}
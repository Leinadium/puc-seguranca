package diretorio;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import javax.crypto.*;


public class Arquivo {
    private final Path arqEnv;
    private final Path arqEnc;
    private final Path arqAsd;

    private static Path dirBase;

    public static void setBase(String pathBase) {
        dirBase = Paths.get(pathBase);
    }

    public static String getBase() { return dirBase.toString(); }

    /** Inicializa um arquivo no cofre digital. */
    public Arquivo(String nome) throws FileNotFoundException {
        this.arqEnv = dirBase.resolve(nome + ".env");
        this.arqEnc = dirBase.resolve(nome + ".enc");
        this.arqAsd = dirBase.resolve(nome + ".asd");

        // verificando existencia dos arquivos
        if (!this.arqEnv.toFile().exists()) {
            throw new FileNotFoundException("Arquivo " + this.arqEnv + " nao existe.");
        }
        if (!this.arqEnc.toFile().exists()) {
            throw new FileNotFoundException("Arquivo " + this.arqEnc + " nao existe.");
        }
        if (!this.arqAsd.toFile().exists()) {
            throw new FileNotFoundException("Arquivo " + this.arqAsd + " nao existe.");
        }
    }

    /** Decripta um arquivo do cofre digital. */
    public byte[] decriptaArquivo(PrivateKey pk) throws Exception {
        // inicializando os objetos
        Cipher cipherDes = Cipher.getInstance("DES/ECB/PKCS5Padding");
        Cipher cipherRsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        KeyGenerator kg = KeyGenerator.getInstance("DES");

        // decriptando o arquivo .env
        cipherRsa.init(Cipher.DECRYPT_MODE, pk);
        byte[] semente = cipherRsa.doFinal(Files.readAllBytes(this.arqEnv));

        // gerando a chave simetrica
        random.setSeed(semente);
        kg.init(56, random);
        SecretKey simKey = kg.generateKey();

        // decriptando o arquivo .enc
        cipherDes.init(Cipher.DECRYPT_MODE, simKey);
        return cipherDes.doFinal(Files.readAllBytes(this.arqEnc));
    }

    /** Valida a assinatura de um arquivo do cofre digital. */
    public boolean autenticidadeArquivo(byte[] textoPlano, PublicKey pubkey) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pubkey);
        sig.update(textoPlano);
        return sig.verify(Files.readAllBytes(this.arqAsd));
    }
}

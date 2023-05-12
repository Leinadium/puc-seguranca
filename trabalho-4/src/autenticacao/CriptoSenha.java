package autenticacao;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;

public class CriptoSenha {
    public static String encripta(String senhaDig) {
        int cost = 8;
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return OpenBSDBCrypt.generate(senhaDig.toCharArray(), salt, cost);
    }

    public static boolean compara(String senhaBD, String senhaDig) {
        return OpenBSDBCrypt.checkPassword(senhaBD, senhaDig.toCharArray());
    }
}

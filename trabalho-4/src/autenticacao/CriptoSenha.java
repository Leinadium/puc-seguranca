package autenticacao;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class CriptoSenha {
    public static String encripta(String senhaDig, byte[] salt) {
        int cost = 8;
        return OpenBSDBCrypt.generate(senhaDig.toCharArray(), salt, cost);
    }

    public static boolean compara(String senhaBD, String senhaDig) {
        return OpenBSDBCrypt.checkPassword(senhaBD, senhaDig.toCharArray());
    }
}

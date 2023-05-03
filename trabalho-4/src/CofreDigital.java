import diretorio.Diretorio;

import java.io.File;
import java.security.PrivateKey;

public class CofreDigital {
    public static void main(String[] args) throws Exception {
        Diretorio dir = new Diretorio("teste");
        File f = new File("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-pkcs8-des.key");

        PrivateKey pk = dir.restauraChavePrivada(f.toPath(), "admin");

        File env = new File("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files/index.env");
        File enc = new File("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files/index.enc");
        File asd = new File("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files/index.asd");

        System.out.println(dir.decriptaArquivo(env.toPath(), enc.toPath(), asd.toPath(), pk));
    }
}

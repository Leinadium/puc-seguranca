import diretorio.Diretorio;
import diretorio.Restaurador;
import basedados.Conexao;
import terminal.InterfaceTerminal;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CofreDigital {
    public static void main(String[] args) throws Exception {
        // Conexao conexao = Conexao.getInstance();

        InterfaceTerminal.mostrarInicio();


        Diretorio dir = new Diretorio("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files");

        String pathPk = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-pkcs8-des.key";
        String pathCert = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-x509.crt";

        PrivateKey privateKey = Restaurador.restauraChavePrivada(pathPk, "admin");
        PublicKey publicKey = Restaurador.restauraChavePublica(pathCert);
        dir.init(privateKey, publicKey);
        dir.show();
    }
}

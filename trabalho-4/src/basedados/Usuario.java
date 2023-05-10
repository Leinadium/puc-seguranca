package basedados;

import java.security.PrivateKey;
import java.security.PublicKey;

public class Usuario {
    public String login;
    public String grupo;
    public String nome;
    public int totalAcessos;
    public String fraseSecreta;

    public PrivateKey privateKey;   // TODO: remover
    public PublicKey publicKey;     // TOOD: remover

    public Usuario() {
        this.login = "";
        this.grupo = "";
        this.nome = "";
        this.fraseSecreta = "";
        this.totalAcessos = 0;
    }
}

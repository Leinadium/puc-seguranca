package basedados;

public class Usuario {
    public String login;
    public String grupo;
    public String nome;
    public int totalAcessos;
    public String fraseSecreta;

    public Usuario() {
        this.login = "";
        this.grupo = "";
        this.nome = "";
        this.fraseSecreta = "";
        this.totalAcessos = 0;
    }
}

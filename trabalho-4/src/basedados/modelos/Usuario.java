package basedados.modelos;

public class Usuario {
    /** identificador do usuario */
    public int uid;

    /** login name do usuario */
    public String loginName;

    /** nome do usuario */
    public String nome;

    /** quantidade de acessos */
    public int numAcessos;

    /** se o usuario esta bloqueado ou nao */
    public int bloqueado;

    /** frase secreta (para certificado?) */
    public String fraseSecreta;

    /** senha criptografada */
    public byte[] senha;

    /** semente para decriptar a senha */
    public String semente;

    /** chaveiro do usuario */
    public Chaveiro chaveiro;

    /** grupo do usuario */
    public Grupo grupo;

    public Usuario() {
        this.uid = 0;
        this.loginName = "";
        this.nome = "";
        this.fraseSecreta = "";
        this.bloqueado = 0;
        this.senha = null;
        this.semente = "";
        this.chaveiro = null;
        this.grupo = null;
    }
}

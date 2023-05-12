package basedados.modelos;

import java.sql.Date;

public class Usuario {
    /** identificador do usuario */
    public int uid;

    /** login name do usuario */
    public String loginName;

    /** nome do usuario */
    public String nome;

    /** quantidade de acessos */
    public int numAcessos;

    /** a hora de bloqueio do usuario */
    public Date bloqueado;

    /** frase secreta (para certificado?) */
    public String fraseSecreta;

    /** senha criptografada */
    public byte[] senha;

    /** semente para decriptar a senha */
    public byte[] semente;

    /** chaveiro do usuario */
    public Chaveiro chaveiro;

    /** grupo do usuario */
    public Grupo grupo;

    public Usuario() {
        this.uid = 0;
        this.loginName = "";
        this.nome = "";
        this.fraseSecreta = "";
        this.bloqueado = null;
        this.senha = null;
        this.semente = null;
        this.chaveiro = null;
        this.grupo = null;
    }
}

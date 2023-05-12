package registro;

import basedados.Conexao;

/** Implementacao de um logger. */
public class Registrador {
    private static Registrador instance;

    private Conexao conexao;

    Registrador() {}

    /** Retorna a instancia do logger. */
    public static Registrador getInstance() {
        if (instance == null) {
            instance = new Registrador();
        }
        return instance;
    }

    public void setConexao(Conexao conexao) {
        this.conexao = conexao;
    }

    public void fazerRegistro(EnumRegistro registro) {
        fazerRegistro(registro, "", "");
    }

    public void fazerRegistro(EnumRegistro registro, String nome) {
        fazerRegistro(registro, nome, "");
    }
    public void fazerRegistro(EnumRegistro registro, String nome, String arquivo) {
        this.conexao.setRegistro(registro.codigo, nome, arquivo);
        // System.out.println("registro" + registro.codigo + " nome " + nome + " arquivo " + arquivo);
    }
}

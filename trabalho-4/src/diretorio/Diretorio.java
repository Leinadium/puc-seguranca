package diretorio;

import basedados.Usuario;
import registro.Registrador;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Diretorio {
    Registrador logger;
    Arquivo arqIndice;

    ArrayList<LinhaIndice> linhasIndice;

    public Diretorio(String path) {
        Arquivo.setBase(path);
        this.logger = Registrador.getInstance();
    }

    public String getPath() {
        return Arquivo.getBase();
    }

    private void parseIndice(String texto) {
        this.linhasIndice = new ArrayList<>();
        String[] linhas = texto.split("\n");
        for (String linha : linhas) {
            String[] campos = linha.split(" ");
            LinhaIndice linhaIndice = new LinhaIndice(campos[0], campos[1], campos[2], campos[3]);
            this.linhasIndice.add(linhaIndice);
        }
    }

    /**
     * Inicializa o diretorio passando as configuracoes necessarias para
     * decriptar e validar o arquivo
     */
    public void init(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        this.arqIndice = new Arquivo("index");
        byte[] textoPlanoBytes = this.arqIndice.decriptaArquivo(privateKey);
        String textoPlano = new String(textoPlanoBytes);

        // System.out.println(textoPlano);
        // this.arqIndice.debug(textoPlanoBytes, publicKey, privateKey);

        if (this.arqIndice.autenticidadeArquivo(textoPlanoBytes, publicKey)) {
            this.parseIndice(textoPlano);
        } else {
            throw new Exception("Arquivo de indice invalido");
        }
    }

    public ArrayList<LinhaIndice> getLinhasUsuario(Usuario usuario) {
        ArrayList<LinhaIndice> ret = new ArrayList<>();
        for (LinhaIndice linha : this.linhasIndice) {
            if (linha.grupo.equals(usuario.grupo) || linha.usuario.equals(usuario.login)) {
                ret.add(linha);
            }
        }
        return ret;
    }
}

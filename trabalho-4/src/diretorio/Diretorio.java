package diretorio;

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
        String textoPlano = this.arqIndice.decriptaArquivo(privateKey);
        System.out.println(textoPlano);
        if (this.arqIndice.autenticidadeArquivo(textoPlano, publicKey)) {
            this.parseIndice(textoPlano);
        } else {
            throw new Exception("Arquivo de indice invalido");
        }
    }

    public void show() {
        for (LinhaIndice linha : this.linhasIndice) {
            System.out.println(linha.codigo+linha.nomeArquivo+linha.usuario+linha.grupo);
        }
    }
}

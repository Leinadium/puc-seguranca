package diretorio;

import basedados.modelos.Usuario;
import registro.EnumRegistro;
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
    public void init(PrivateKey privateKey, PublicKey publicKey, Registrador registrador, Usuario usuario) throws Exception {
        try {
            this.arqIndice = new Arquivo("index");
        } catch (Exception e) {
            registrador.fazerRegistro(EnumRegistro.CAMINHO_PASTA_INVALIDO, usuario.loginName);
            throw new Exception("Arquivo de indice nao encontrado");
        }
        byte[] textoPlanoBytes;
        try {
            registrador.fazerRegistro(EnumRegistro.ARQUIVO_INDICE_DECRIPTADO_SUCESSO, usuario.loginName);
            textoPlanoBytes = this.arqIndice.decriptaArquivo(privateKey);
        } catch (Exception e) {
            registrador.fazerRegistro(EnumRegistro.ARQUIVO_INDICE_DECRIPTADO_FALHA, usuario.loginName);
            throw new Exception("Arquivo de indice invalido");
        }
        String textoPlano = new String(textoPlanoBytes);
        try {
            if (this.arqIndice.autenticidadeArquivo(textoPlanoBytes, publicKey)) {
                this.parseIndice(textoPlano);
                registrador.fazerRegistro(EnumRegistro.ARQUIVO_INDICE_VALIDADO_SUCESSO, usuario.loginName);
            } else {
                throw new Exception("Arquivo de indice invalido");
            }
        } catch (Exception e) {
            registrador.fazerRegistro(EnumRegistro.ARQUIVO_INDICE_VALIDADO_FALHA, usuario.loginName);
            throw e;
        }
    }

    public ArrayList<LinhaIndice> getLinhasUsuario(Usuario usuario) {
        ArrayList<LinhaIndice> ret = new ArrayList<>();
        for (LinhaIndice linha : this.linhasIndice) {
            if (linha.grupo.equals(usuario.grupo.nome) || linha.usuario.equals(usuario.loginName)) {
                ret.add(linha);
            }
        }
        return ret;
    }
}

import basedados.Usuario;
import diretorio.Arquivo;
import diretorio.Diretorio;
import diretorio.LinhaIndice;
import diretorio.Restaurador;
import basedados.Conexao;
import terminal.FormularioCadastro;
import terminal.InterfaceTerminal;
import terminal.Operacao;

import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class CofreDigital {

    Diretorio diretorio;
    Usuario usuario;
    Conexao conexao;

    // temporario
    PublicKey adminPublicKey;       // TODO: remover
    PrivateKey adminPrivateKey;     // TODO: remover

    boolean fecharSistema = false;

    public static void main(String[] args) throws Exception {
        // Conexao conexao = Conexao.getInstance();
        mainFinal("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files");

    }

    public static void mainFinal(String dirPath) {
        CofreDigital cofreDigital = new CofreDigital();
        cofreDigital.diretorio = new Diretorio(dirPath);

        try {
            cofreDigital.conexao = Conexao.getInstance();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            // return;  // TODO: voltar com o return
        }

        // TODO: primeiro cadastro do admin

        while (!cofreDigital.fecharSistema) {
            cofreDigital.loopAutenticacao();
            cofreDigital.loopSistema();
        }
    }

    private void loopAutenticacao() {
        // TODO: autenticacao do usuario
        this.usuario = new Usuario();
        this.usuario.login = "admin@inf1416.puc-rio.br";
        this.usuario.grupo = "admin";
        this.usuario.nome = "Daniel";
        this.usuario.fraseSecreta = "admin";

        // TODO: REMOVER CHAVES DAQUI, SOMENTE PARA TESTE
        String pathPk = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-pkcs8-des.key";
        String pathCert = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-x509.crt";

        try {
            this.adminPrivateKey = Restaurador.restauraChavePrivada(pathPk, "admin");
            this.adminPublicKey = Restaurador.restauraChavePublica(pathCert);

            this.usuario.privateKey = Restaurador.restauraChavePrivada(pathPk, "admin");
            this.usuario.publicKey = Restaurador.restauraChavePublica(pathCert);

        } catch (Exception e) {
            System.out.println("Erro ao restaurar chaves.");
        }
    }

    private void loopSistema() {
        boolean sair = false;
        while (!sair) {
            Operacao operacao = InterfaceTerminal.menuPrincipal(this.usuario);
            switch (operacao) {
                case CADASTRAR_NOVO_USUARIO:
                    this.cadastrarNovoUsuario();
                    break;
                case CONSULTAR_PASTA:
                    this.consultarPasta();
                    break;
                case SAIR_SISTEMA:
                    sair = this.processarSaida();
            }
        }

        // resetando usuario
        this.usuario = null;
    }

    private void cadastrarNovoUsuario() {
        FormularioCadastro formularioCadastro = InterfaceTerminal.mostrarFormularioCadastro(this.usuario);
        if (formularioCadastro == null) {
            // TODO
        }
        // TODO: cadastrar novo usuario no banco
    }

    private void consultarPasta() {
        String erro = "";
        ArrayList<LinhaIndice> linhas = null;
        int r = -2;
        while (r != -1) {   // -1 = sair
            r = InterfaceTerminal.consultarPasta(this.usuario, this.diretorio, linhas, erro);
            if (r == 0) {   // mostrar pasta

                // TODO: validar frase secreta do usuario

                try {
                    this.diretorio.init(this.adminPrivateKey, this.adminPublicKey);
                } catch (Exception e) {
                    erro = "ERRO INICIALIZANDO DIRETORIO (" + e.getMessage() + ")";
                    continue;
                }
                linhas = this.diretorio.getLinhasUsuario(this.usuario);
            } else if (r > 0 && linhas != null) {
                LinhaIndice linha = linhas.get(r - 1);

                // verificando se o arquivo eh do usuario
                if (!linha.usuario.equals(this.usuario.login)) {
                    erro = "ERRO ACESSANDO ARQUIVO (ARQUIVO NAO PERTENCE AO USUARIO)";
                    continue;
                }

                // abrindo arquivo
                Arquivo arq;
                try {
                    arq = new Arquivo(linha.codigo);
                } catch (Exception e) {
                    erro = "ERRO ABRINDO ARQUIVO (" + e.getMessage() + ")";
                    continue;
                }
                // decriptando e validando
                byte[] conteudo;
                try {
                    conteudo = arq.decriptaArquivo(this.adminPrivateKey);
                    if (!arq.autenticidadeArquivo(conteudo, this.adminPublicKey)) {
                        erro = "ERRO VALIDANDO ASSINATURA (ASSINATURA INVALIDA)";
                        continue;
                    }
                } catch (Exception e) {
                    erro = "ERRO DECRIPTANDO ARQUIVO (" + e.getMessage() + ")";
                    continue;
                }
                try {
                    try (FileOutputStream stream = new FileOutputStream(linha.nomeArquivo)) {
                        stream.write(conteudo);
                    }
                    // utilizando o erro como mensagem de sucesso... :P
                    erro = "ARQUIVO (" + linha.nomeArquivo + ") SALVO COM SUCESSO";
                } catch (Exception e) {
                    erro = "ERRO SALVANDO ARQUIVO DECRIPTADO (" + e.getMessage() + ")";
                }

            }
        }

    }

    /** retorna true se deve encerrar. altera .fecharSistema se deve fechar tudo */
    private boolean processarSaida() {
        switch (InterfaceTerminal.telaSaida(this.usuario)) {
            case SAIR_SISTEMA:
                this.fecharSistema = true;
                return true;
            case ENCERRAR_SESSAO:
                return true;
            case RETORNAR_MENU:
                return false;
        }
        return false;   // nunca deve cair aqui (espero)
    }
}

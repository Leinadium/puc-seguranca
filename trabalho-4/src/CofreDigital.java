import basedados.modelos.Chaveiro;
import basedados.modelos.Usuario;
import criptografia.CriptoToken;
import criptografia.Restaurador;
import diretorio.*;
import basedados.Conexao;
import terminal.FormularioCadastro;
import terminal.InterfaceTerminal;
import terminal.Operacao;

import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;

public class CofreDigital {
    Diretorio diretorio;
    Usuario usuario;
    Conexao conexao;
    InfoAdmin infoAdmin;
    boolean fecharSistema;

    public static void main(String[] args) throws Exception {
        CofreDigital cofre = new CofreDigital("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files");
        cofre.mainFinal();
    }

    public CofreDigital(String dirPath) {
        this.diretorio = new Diretorio(dirPath);
        this.usuario = null;
        this.conexao = null;
        this.infoAdmin = new InfoAdmin();
        this.fecharSistema = false;
    }

    public void mainFinal() {
        // inicia conexao com o banco
        try {
            this.conexao = Conexao.getInstance();
            this.conexao.criar();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            return;
        }

        try {
            Chaveiro chaveiroAdmin = this.conexao.chaveiroAdmin();
            if (chaveiroAdmin == null) {        // CADASTRO DO ADMIN!!
                // pegando formulario
                FormularioCadastro form = InterfaceTerminal.mostrarFormularioCadastro(null, "");
                if (form == null) {
                    System.out.println("Fechando sistema (cadastro abortado");
                    return;
                }

                // pegando certificado
                Certificate cert = Restaurador.restauraCertificado(form.pathCert);
                CertificadoInfo certInfo = CertificadoInfo.fromCertificado(cert);
                if (InterfaceTerminal.verificarCertificadoInvalido(certInfo)) {
                    System.out.println("Fechando sistema (certificado invalido)");
                    return;
                }

                // pegando bytes da private key
                byte[] chavePrivadaBytes = Restaurador.restauraChavePrivadaBytes(form.pathPk);

                // criando admin
                Usuario admin = new Usuario();
                admin.loginName = certInfo.emailSujeito;
                admin.nome = certInfo.nomeSujeito;
                // usuario.fraseSecreta = form.fraseSecreta;        // NAO PODE SALVAR SENHA DO ADMIN
                admin.senha = "kkkk";        // TODO: senha
                admin.bloqueado = null;
                admin.semente = CriptoToken.geraSemente(form.senhaPessoal);
                admin.grupo = this.conexao.getGrupo(form.grupo);
                admin.chaveiro = new Chaveiro();
                admin.chaveiro.chavePrivadaBytes = chavePrivadaBytes;
                admin.chaveiro.chavePublicaPem = Restaurador.geraChavePublicaPem(cert.getPublicKey());
                this.conexao.setUsuario(admin);

                // validando (sera jogada uma excecao se der errado)
                Restaurador.restauraChavePrivada(
                        admin.chaveiro.chavePrivadaBytes,
                        form.fraseSecreta
                );

                // armazenando informacoes do adm
                this.infoAdmin.set(
                        form.fraseSecreta,  // SALVANDO EM MEMORIA
                        admin.chaveiro.chavePrivadaBytes,
                        admin.chaveiro.chavePublicaPem
                );
            } else {
                String erro = "";
                do {
                    String fraseTalvez = InterfaceTerminal.pedeFraseAdmin(erro);
                    // tentando validar a chave
                    try {
                        this.infoAdmin.set(
                                fraseTalvez,    // SALVANDO EM MEMORIA
                                chaveiroAdmin.chavePrivadaBytes,
                                chaveiroAdmin.chavePublicaPem
                        );
                        erro = "";
                    } catch (Exception e) {
                        erro = "Frase inválida (" + e.getMessage() + ")";
                    }
                } while (!erro.equals(""));
            }

        } catch (Exception e) {
            System.out.println("Erro na primeira execucao do sistema: " + e.getMessage());
            return;
        }

        while (!this.fecharSistema) {
            this.loopAutenticacao();
            this.loopSistema();
        }
    }

    private void loopAutenticacao() {
        // TODO: autenticacao do usuario
        try {
            this.usuario = this.conexao.getUsuario("admin@inf1416.puc-rio.br");
        } catch (Exception e) {
            System.out.println("Erro ao pegar usuario: " + e.getMessage());
            System.exit(1);
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
        String erro = "";
        while (true) {
            FormularioCadastro form = InterfaceTerminal.mostrarFormularioCadastro(this.usuario, erro);
            if (form == null) {
                // cadastro abortado
                break;
            }
            // pegando o certificado
            Certificate cert;
            CertificadoInfo certInfo;
            try {
                cert = Restaurador.restauraCertificado(form.pathCert);
                certInfo = CertificadoInfo.fromCertificado(cert);
                if (InterfaceTerminal.verificarCertificadoInvalido(certInfo)) {
                    // cadastro recusado
                    continue;
                }
            } catch (Exception e) {
                erro = "Certificado invalido (" + e.getMessage() + ")";
                continue;
            }

            // pegando a chave privada
            byte[] chavePrivadaBytes;
            try {
                chavePrivadaBytes = Restaurador.restauraChavePrivadaBytes(form.pathPk);
            } catch (Exception e) {
                erro = "Chave privada invalida (" + e.getMessage() + ")";
                continue;
            }

            // criando usuario
            Usuario usuario = new Usuario();
            usuario.loginName = certInfo.emailSujeito;
            try {
                if (this.conexao.existeLoginName(usuario.loginName)) {
                    erro = "Login name já existe";
                    continue;
                }
            } catch (Exception e) {
                erro = "Erro ao verificar login name (" + e.getMessage() + ")";
                continue;
            }

            usuario.nome = certInfo.nomeSujeito;
            usuario.fraseSecreta = form.fraseSecreta;
            usuario.senha = form.senhaPessoal;   // TODO: senha
            usuario.bloqueado = null;
            usuario.semente = CriptoToken.geraSemente(form.senhaPessoal);
            try {      // PEGANDO GRUPO
                usuario.grupo = this.conexao.getGrupo(form.grupo);
            } catch (Exception e) {
                erro = "Grupo inválida ou banco de dados inválido (" + e.getMessage() + ")";
                continue;
            }
            usuario.chaveiro = new Chaveiro();
            usuario.chaveiro.chavePrivadaBytes = chavePrivadaBytes;
            PrivateKey privk;
            PublicKey pubk = cert.getPublicKey();
            try {       // PEGANDO CHAVE PUBLICA
                usuario.chaveiro.chavePublicaPem = Restaurador.geraChavePublicaPem(pubk);
            } catch (Exception e) {
                erro = "Chave pública inválida (" + e.getMessage() + ")";
                continue;
            }
            try {       // PEGANDO CHAVE PRIVADA
                privk = Restaurador.restauraChavePrivada(
                        usuario.chaveiro.chavePrivadaBytes,
                        usuario.fraseSecreta
                );
            } catch (Exception e) {
                erro = "Frase secreta inválida (" + e.getMessage() + ")";
                continue;
            }
            try {       // VALIDANDO CHAVES
                if (!Restaurador.testaChaves(privk, pubk)) {
                    erro = "Chaves privada e pública não combinam";
                    continue;
                }
            } catch (Exception e) {
                erro = "Erro ao testar chaves (" + e.getMessage() + ")";
                continue;
            }

            try {       // SALVANDO USUARIO
                this.conexao.setUsuario(usuario);
            } catch (Exception e) {
                erro = "Erro ao gravar usuário (" + e.getMessage() + ")";
                continue;
            }

            break;
        }

    }

    private void consultarPasta() {
        String erro = "";
        ArrayList<LinhaIndice> linhas = null;
        int r = -2;
        while (r != -1) {   // -1 = sair
            r = InterfaceTerminal.consultarPasta(this.usuario, this.diretorio, linhas, erro);
            if (r == 0) {   // mostrar pasta
                // TODO: validar frase secreta do usuario (ue ja nao foi verificada??)

                try {
                    this.diretorio.init(this.infoAdmin.getPrivateKey(), this.infoAdmin.getPublicKey());
                } catch (Exception e) {
                    erro = "ERRO INICIALIZANDO DIRETORIO (" + e.getMessage() + ")";
                    continue;
                }
                linhas = this.diretorio.getLinhasUsuario(this.usuario);
            } else if (r > 0 && linhas != null) {
                LinhaIndice linha = linhas.get(r - 1);

                // verificando se o arquivo eh do usuario
                if (!linha.usuario.equals(this.usuario.loginName)) {
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
                    PrivateKey pk;
                    PublicKey pubKey;
                    try {
                        pk = Restaurador.restauraChavePrivada(
                                this.usuario.chaveiro.chavePrivadaBytes,
                                this.usuario.fraseSecreta
                        );
                    } catch (Exception e) {
                        erro = "ERRO RESTAURANDO CHAVE PRIVADA (" + e.getMessage() + ")";
                        continue;
                    }
                    try {
                        pubKey = Restaurador.getChavePublica(this.usuario.chaveiro.chavePublicaPem);
                    } catch (Exception e) {
                        erro = "ERRO RESTAURANDO CHAVE PUBLICA (" + e.getMessage() + ")";
                        continue;
                    }

                    conteudo = arq.decriptaArquivo(pk);
                    if (!arq.autenticidadeArquivo(conteudo, pubKey)) {
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


class InfoAdmin {

    private String frase;

    private byte[] priv;

    private String pub;

    public void set(String frase, byte[] priv, String pub) {
        this.frase = frase;
        this.priv = priv;
        this.pub = pub;
    }

    public PrivateKey getPrivateKey() throws Exception {
        return Restaurador.restauraChavePrivada(this.priv, this.frase);
    }


    public PublicKey getPublicKey() throws Exception {
        return Restaurador.getChavePublica(this.pub);
    }
}
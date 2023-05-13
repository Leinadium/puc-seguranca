import autenticacao.Login;
import criptografia.CriptoSenha;
import basedados.modelos.Chaveiro;
import basedados.modelos.Usuario;
import criptografia.CriptoToken;
import criptografia.Restaurador;
import diretorio.*;
import basedados.Conexao;
import registro.Registrador;
import registro.EnumRegistro;
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

    Registrador registrador;
    boolean fecharSistema;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Uso: java CofreDigital <diretorio>");
            System.exit(1);
        }
        CofreDigital cofre = new CofreDigital(args[0]);
        cofre.mainFinal();
    }

    public CofreDigital(String dirPath) {
        this.diretorio = new Diretorio(dirPath);
        this.registrador = Registrador.getInstance();
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
        this.registrador.setConexao(this.conexao);
        this.registrador.fazerRegistro(EnumRegistro.SISTEMA_INICIADO);

        try {
            Chaveiro chaveiroAdmin = this.conexao.chaveiroAdmin();
            if (chaveiroAdmin == null) {        // CADASTRO DO ADMIN!!
                // pegando formulario
                FormularioCadastro form = InterfaceTerminal.mostrarFormularioCadastro(null, "", this.registrador, 0);
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
                admin.fraseSecreta = form.fraseSecreta;     // o banco nao armazena a frase secreta
                admin.senha = CriptoSenha.encripta(form.senhaPessoal);
                admin.bloqueado = null;
                admin.semente = CriptoToken.geraSemente(form.senhaPessoal);
                admin.grupo = this.conexao.getGrupo(form.grupo);
                admin.numAcessos = 0;
                admin.chaveiro = new Chaveiro();
                admin.chaveiro.chavePrivadaBytes = chavePrivadaBytes;
                admin.chaveiro.chavePublicaPem = Restaurador.geraChavePublicaPem(cert.getPublicKey());

                // validando (sera jogada uma excecao se der errado)
                Restaurador.restauraChavePrivada(
                        admin.chaveiro.chavePrivadaBytes,
                        form.fraseSecreta
                );

                this.conexao.setUsuario(admin);

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

        this.registrador.fazerRegistro(EnumRegistro.SISTEMA_ENCERRADO);
    }

    private void loopAutenticacao() {
        Usuario usuario = null;
        while (usuario == null) {
            usuario = Login.login();
        }
        this.usuario = usuario;

        this.usuario.numAcessos++;
        this.conexao.atualizarNumeroAcessos(this.usuario);

        // se o usuario for admin, this.usuario.fraseSecreta eh nulo, pq eu nao posso salvar a frase secreta do admin
        // pelo menos foi o que eu entendi do enunciado
        // entao eu tenho que pegar do adminInfo.fraseSecreta
        if (this.usuario.grupo.nome.equals("administrador")) {
            this.usuario.fraseSecreta = this.infoAdmin.getFrase();
        }
    }

    private void loopSistema() {
        boolean sair = false;
        while (!sair) {
            this.registrador.fazerRegistro(EnumRegistro.TELA_PRINCIPAL, this.usuario.loginName);

            Operacao operacao = InterfaceTerminal.menuPrincipal(this.usuario);
            switch (operacao) {
                case CADASTRAR_NOVO_USUARIO:
                    this.registrador.fazerRegistro(EnumRegistro.MENU_1_SELECIONADO, this.usuario.loginName);
                    this.cadastrarNovoUsuario();
                    break;
                case CONSULTAR_PASTA:
                    this.registrador.fazerRegistro(EnumRegistro.MENU_2_SELECIONADO, this.usuario.loginName);
                    this.consultarPasta();
                    break;
                case SAIR_SISTEMA:
                    this.registrador.fazerRegistro(EnumRegistro.MENU_3_SELECIONADO, this.usuario.loginName);
                    sair = this.processarSaida();
            }
        }
        // resetando usuario
        this.usuario = null;
    }

    private void cadastrarNovoUsuario() {
        String erro = "";
        this.registrador.fazerRegistro(EnumRegistro.TELA_CADASTRO, this.usuario.loginName);

        // pegando a quantidade de usuarios no sistema
        int quantidadeUsuarios = conexao.quantidadeUsuarios();
        while (true) {
            FormularioCadastro form = InterfaceTerminal.mostrarFormularioCadastro(this.usuario, erro, registrador, quantidadeUsuarios);
            if (form == null) {
                // cadastro abortado
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_VOLTAR_CADASTRO_SELECIONADO);
                break;
            } else {
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_CADASTRO_SELECIONADO);
            }
            // pegando o certificado
            Certificate cert;
            CertificadoInfo certInfo;
            try {
                cert = Restaurador.restauraCertificado(form.pathCert);
                certInfo = CertificadoInfo.fromCertificado(cert);
                if (InterfaceTerminal.verificarCertificadoInvalido(certInfo)) {
                    // cadastro recusado
                    this.registrador.fazerRegistro(EnumRegistro.CONFIRMACAO_DADOS_REJEITADA, usuario.loginName);
                    continue;
                } else {
                    this.registrador.fazerRegistro(EnumRegistro.CONFIRMACAO_DADOS_ACEITA, usuario.loginName);
                }
            } catch (Exception e) {
                this.registrador.fazerRegistro(EnumRegistro.CAMINHO_CERTIFICADO_INVALIDO, usuario.loginName);
                erro = "Certificado invalido (" + e.getMessage() + ")";
                continue;
            }

            // pegando a chave privada
            byte[] chavePrivadaBytes;
            try {
                chavePrivadaBytes = Restaurador.restauraChavePrivadaBytes(form.pathPk);
            } catch (Exception e) {
                this.registrador.fazerRegistro(EnumRegistro.CHAVE_PRIVADA_INVALIDA_CAMINHO_INVALIDO, usuario.loginName);
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
            usuario.senha = CriptoSenha.encripta(form.senhaPessoal);
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
                this.registrador.fazerRegistro(EnumRegistro.CHAVE_PRIVADA_INVALIDA_FRASE_INVALIDA, usuario.loginName);
                erro = "Frase secreta inválida (" + e.getMessage() + ")";
                continue;
            }
            try {       // VALIDANDO CHAVES
                if (!Restaurador.testaChaves(privk, pubk)) {
                    this.registrador.fazerRegistro(EnumRegistro.CHAVE_PRIVADA_INVALIDA_ASSINATURA_INVALIDA, usuario.loginName);
                    erro = "Chaves privada e pública não combinam";
                    continue;
                }
            } catch (Exception e) {
                this.registrador.fazerRegistro(EnumRegistro.CHAVE_PRIVADA_INVALIDA_ASSINATURA_INVALIDA, usuario.loginName);
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

        this.registrador.fazerRegistro(EnumRegistro.TELA_CONSULTA, this.usuario.loginName);
        while (r != -1) {   // -1 = sair
            r = InterfaceTerminal.consultarPasta(this.usuario, this.diretorio, linhas, erro);
            if (r == 0) {   // mostrar pasta
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_LISTAR_CONSULTA_SELECIONADO, this.usuario.loginName);

                try {
                    this.diretorio.init(
                            this.infoAdmin.getPrivateKey(),
                            this.infoAdmin.getPublicKey(),
                            this.registrador,
                            this.usuario
                    );
                } catch (Exception e) {
                    // TODOS OS REGISTROS FORAM DEVIDAMENTE FEITOS
                    erro = "ERRO INICIALIZANDO DIRETORIO (" + e.getMessage() + ")";
                    continue;
                }
                linhas = this.diretorio.getLinhasUsuario(this.usuario);
                this.registrador.fazerRegistro(EnumRegistro.LISTA_ARQUIVOS_PRESENTE, this.usuario.loginName);

            } else if (r > 0 && linhas != null) {
                LinhaIndice linha = linhas.get(r - 1);
                // verificando se o arquivo eh do usuario
                if (!linha.usuario.equals(this.usuario.loginName)) {
                    this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_ACESSO_NEGADO, this.usuario.loginName, linha.codigo);
                    erro = "ERRO ACESSANDO ARQUIVO (ARQUIVO NAO PERTENCE AO USUARIO)";
                    continue;
                } else {
                    this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_ACESSO_PERMITIDO, this.usuario.loginName, linha.codigo);
                }
                // abrindo arquivo
                Arquivo arq;
                try {
                    arq = new Arquivo(linha.codigo);
                } catch (Exception e) {
                    // NAO EXISTE MENSAGEM DE ERRO PARA ESSE TIPO DE FALHA (FALTA OS ARQUIVOS .env, .asd, ...)
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
                        // NAO EXISTE MENSAGEM DE ERRO (SOMENTE POSSIVEL SE O BANCO FOI INVALIDADO)
                        // OBS: MENTIRA, TEM SIM, ELE PEDIU
                        this.registrador.fazerRegistro(EnumRegistro.CHAVE_PRIVADA_INVALIDA_FRASE_INVALIDA);
                        erro = "ERRO RESTAURANDO CHAVE PRIVADA (CHAVE INVALIDA?) (" + e.getMessage() + ")";
                        continue;
                    }
                    try {
                        pubKey = Restaurador.getChavePublica(this.usuario.chaveiro.chavePublicaPem);
                    } catch (Exception e) {
                        // NAO EXISTE MENSAGEM DE ERRO (SOMENTE POSSIVEL SE O BANCO FOI INVALIDADO)
                        erro = "ERRO RESTAURANDO CHAVE PUBLICA (" + e.getMessage() + ")";
                        continue;
                    }

                    conteudo = arq.decriptaArquivo(pk);
                    this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_DECRIPTADO_SUCESSO, this.usuario.loginName, linha.codigo);
                    if (!arq.autenticidadeArquivo(conteudo, pubKey)) {
                        this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_VALIDADO_FALHA, this.usuario.loginName, linha.codigo);
                        erro = "ERRO VALIDANDO ASSINATURA (ASSINATURA INVALIDA)";
                        continue;
                    } else {
                        this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_VALIDADO_SUCESSO, this.usuario.loginName, linha.codigo);
                    }
                } catch (Exception e) {
                    this.registrador.fazerRegistro(EnumRegistro.ARQUIVO_DECRIPTADO_FALHA, this.usuario.loginName, linha.codigo);
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
                    // SEM MENSAGEM DE ERRO PARA ESSE TIPO DE FALHA
                    erro = "ERRO SALVANDO ARQUIVO DECRIPTADO (" + e.getMessage() + ")";
                }
            } else {
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_VOLTAR_CONSULTA_SELECIONADO, this.usuario.loginName);
            }
        }
    }

    /** retorna true se deve encerrar. altera .fecharSistema se deve fechar tudo */
    private boolean processarSaida() {
        this.registrador.fazerRegistro(EnumRegistro.TELA_SAIDA, this.usuario.loginName);
        switch (InterfaceTerminal.telaSaida(this.usuario)) {
            case SAIR_SISTEMA:
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_ENCERRAR_SISTEMA_SELECIONADO, this.usuario.loginName);
                this.fecharSistema = true;
                return true;
            case ENCERRAR_SESSAO:
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_ENCERRAR_SESSAO_SELECIONADO, this.usuario.loginName);
                return true;
            case RETORNAR_MENU:
                this.registrador.fazerRegistro(EnumRegistro.BOTAO_VOLTAR_SAIR_MENU_SELECIONADO, this.usuario.loginName);
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

    public String getFrase() {
        return this.frase;
    }
}
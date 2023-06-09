package terminal;

import basedados.modelos.Usuario;
import diretorio.CertificadoInfo;
import diretorio.LinhaIndice;
import registro.EnumRegistro;
import registro.Registrador;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InterfaceTerminal {
    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void mostrarCabecalho(Usuario usuario) {
        System.out.println("===============================");
        System.out.println("Login: " + usuario.loginName);
        System.out.println("Grupo: " + usuario.grupo.nome);
        System.out.println("Nome: " + usuario.nome);
    }

    public static String pedeFraseAdmin(String erro) {
        Console console = System.console();
        limparTela();
        System.out.println("==================================");
        if (!erro.equals("")) {
            System.out.println("Erro: " + erro);
        }

        System.out.println("Digite a frase secreta do admin: ");
        char[] fraseSecreta = console.readPassword();
        return Arrays.toString(fraseSecreta);
    }

    public static Operacao menuPrincipal(Usuario usuario) {
        int opcao;
        while (true) {
            limparTela();
            mostrarCabecalho(usuario);
            System.out.println("\nTotal de acessos: " + usuario.numAcessos);
            System.out.println("\nMenu principal:");

            boolean isAdmin = Objects.equals(usuario.grupo.nome, "administrador");

            if (isAdmin) {
                System.out.println("\n1 - Cadastrar novo usuário");
                System.out.println("2 - Consultar pasta de arquivos secretos do usuário");
                System.out.println("3 - Sair do sistema");
            } else {
                System.out.println("1 - Consultar pasta de arquivos secretos do usuário");
                System.out.println("2 - Sair do sistema");
            }


            Scanner scanner = new Scanner(System.in);
            try {
                opcao = scanner.nextInt();
                switch (opcao) {
                    case 1:
                        return isAdmin ? Operacao.CADASTRAR_NOVO_USUARIO : Operacao.CONSULTAR_PASTA;
                    case 2:
                        return isAdmin ? Operacao.CONSULTAR_PASTA : Operacao.SAIR_SISTEMA;
                    case 3:
                        if (isAdmin) {
                            return Operacao.SAIR_SISTEMA;
                        }

                }
            } catch (Exception e) { // se o usuario digitar algo que nao eh um numero
                scanner.nextLine();
            }
        }
    }

    public static String esconderSenha(String senha) {
        StringBuilder senhaEscondida = new StringBuilder();
        for (int i = 0; i < senha.length(); i++) {
            senhaEscondida.append("*");
        }
        return senhaEscondida.toString();
    }

    static String validarSenha(String senha) {
        if (!senha.matches("[0-9]{8,10}")) {
            return "A senha deve ter entre 8 e 10 numeros";
        }
        // checando para ver se tem dois numeros iguais seguidos
        for (int i = 0; i < senha.length() - 1; i++) {
            if (senha.charAt(i) == senha.charAt(i + 1)) {
                return "A senha não pode ter dois números iguais seguidos";
            }
        }
        return null;
    }

    public static FormularioCadastro mostrarFormularioCadastro(Usuario usuario, String erro, Registrador registrador, int quantidadeUsuarios) {
        FormularioCadastro formularioCadastro = new FormularioCadastro();
        Scanner scanner = new Scanner(System.in);

        int campoAtual = 0;
        boolean senhaValidada = false;

        while (true) {
            limparTela();
            // se for nulo, eh um registro de um admin
            if (usuario != null) {
                mostrarCabecalho(usuario);
                System.out.println("\nTotal de usuários no sistema: " + quantidadeUsuarios);
            } else {
                System.out.println("===============================");
            }
            System.out.println("\nFormulário de Cadastro:");
            System.out.println("- Caminho do arquivo do certificado digital: " + formularioCadastro.pathCert);
            System.out.println("- Caminho do arquivo da chave privada: " + formularioCadastro.pathPk);
            System.out.println("- Frase secreta: " + formularioCadastro.fraseSecreta);

            if (usuario != null) { System.out.println("- Grupo: " + formularioCadastro.grupo); }
            else { System.out.println("- Grupo: administrador"); }

            System.out.println("- Senha pessoal: " + esconderSenha(formularioCadastro.senhaPessoal));
            if (senhaValidada) {
                System.out.println("- Senha pessoal: " + esconderSenha(formularioCadastro.senhaPessoal));
            } else {
                System.out.println("- Confirmação senha pessoal: ");
            }
            if (!erro.equals("")) {
                System.out.println("\n" + erro);
            }

            Console console;
            switch (campoAtual) {
                case 0:
                    System.out.println("\nDigite o caminho para o certificado digital: ");
                    formularioCadastro.pathCert = scanner.nextLine();
                    if (formularioCadastro.pathCert.length() > 255) {
                        erro = "O caminho do certificado não pode ter mais de 255 caracteres!";
                        formularioCadastro.pathCert = "";
                    } else {
                        erro = "";
                        campoAtual++;
                    }
                    break;
                case 1:
                    System.out.println("\nDigite o caminho para a chave privada do usuário:");
                    formularioCadastro.pathPk = scanner.nextLine();
                    if (formularioCadastro.pathPk.length() > 255) {
                        erro = "O caminho da chave privada não pode ter mais de 255 caracteres!";
                        formularioCadastro.pathPk = "";
                    } else {
                        erro = "";
                        campoAtual++;
                    }
                    break;
                case 2:
                    System.out.println("\nDigite a frase secreta do usuário:");
                    console = System.console();
                    if (console != null) {
                        char[] kkk = console.readPassword();
                        formularioCadastro.fraseSecreta = new String(kkk);
                    } else {
                        formularioCadastro.fraseSecreta = scanner.nextLine();
                    }


                    formularioCadastro.fraseSecreta = scanner.nextLine();
                    if (formularioCadastro.senhaPessoal.length() > 255) {
                        erro = "A senha secreta não pode ter mais de 255 caracteres!";
                        formularioCadastro.fraseSecreta = "";
                    } else {
                        erro = "";
                        campoAtual++;
                        if (usuario == null) {
                            formularioCadastro.grupo = "administrador";
                            campoAtual++;   // pula o registro de administrador
                        }
                    }
                    break;
                case 3:
                    System.out.println("\nDigite o grupo do usuário ([U]ser, [A]dministrador): ");
                    formularioCadastro.grupo = scanner.nextLine();
                    String grupo = formularioCadastro.grupo.toLowerCase();
                    if (grupo.equals("u") || grupo.equals("usuario")) {
                        formularioCadastro.grupo = "usuario";
                        campoAtual++;
                        erro = "";
                    } else if (grupo.equals("a") || grupo.equals("administrador")) {
                        formularioCadastro.grupo = "administrador";
                        campoAtual++;
                        erro = "";
                    } else {
                        formularioCadastro.grupo = "";
                        erro = "Grupo inválido!";
                    }
                    break;
                case 4:
                    System.out.println("\nDigite a senha pessoal do usuário ou SAIR:");
                    console = System.console();
                    if (console != null) {
                        char[] senhaPessoal = console.readPassword();
                        formularioCadastro.senhaPessoal = new String(senhaPessoal);
                    } else {
                        formularioCadastro.senhaPessoal = scanner.nextLine();
                    }
                    String x = validarSenha(formularioCadastro.senhaPessoal);
                    if (x != null) {
                        erro = x;
                        formularioCadastro.senhaPessoal = "";
                        registrador.fazerRegistro(EnumRegistro.SENHA_PESSOAL_INVALIDA, usuario == null ? "adminInicial" : usuario.loginName);
                    } else {
                        erro = "";
                        campoAtual++;
                    }
                    break;
                case 5:
                    System.out.println("\nDigite a senha novamente:");
                    console = System.console();
                    String senha;
                    if (console != null) {
                        char[] senhaPessoal = console.readPassword();
                        senha = new String(senhaPessoal);
                    } else {
                        senha = scanner.nextLine();
                    }
                    if (!Objects.equals(senha, formularioCadastro.senhaPessoal)) {
                        erro = "As senhas não conferem!";
                    } else {
                        erro = "";
                        campoAtual++;
                        senhaValidada = true;
                    }
                    break;
                case 6:
                    System.out.println("\nPressione ENTER para cadastrar ou qualquer outra tecla para cancelar:");
                    String opcao = scanner.nextLine();
                    if (opcao.equals("")) {
                        return formularioCadastro;
                    } else {
                        return null;
                    }
            }
        }
    }

    /** retorna true se o usuario confirmou, false se deseja voltar atras */
    public static boolean verificarCertificadoInvalido(CertificadoInfo certInfo) {
        limparTela();
        System.out.println("===================================");
        System.out.println("Certificado encontrado:");

        System.out.println(" - Versão: " + certInfo.versao);
        System.out.println(" - Número de série: " + certInfo.serie);
        System.out.println(" - Validade: " + certInfo.validade);
        System.out.println(" - Tipo de assinatura: " + certInfo.tipoAssinatura);
        System.out.println(" - Emissor: " + certInfo.nomeEmissor);
        System.out.println(" - Sujeito: " + certInfo.nomeSujeito);
        System.out.println(" - Email: " + certInfo.emailSujeito);

        System.out.println("\nPressione ENTER para confirmar ou qualquer outra tecla para cancelar:");
        Scanner scanner = new Scanner(System.in);
        String opcao = scanner.nextLine();
        return !opcao.equals("");
    }
    /** retorna -1 pra voltar, 0 para abrir o diretorio, e 1-n para a selecao de um arquivo*/
    public static FormularioPasta consultarPasta(Usuario usuario, ArrayList<LinhaIndice> linhas, String erro, FormularioPasta formularioPasta) {
        String opcao;
        Scanner scanner = new Scanner(System.in);
        boolean show = linhas != null;
        if (formularioPasta == null) {
            formularioPasta = new FormularioPasta();
            formularioPasta.caminhoPasta = "";
            formularioPasta.fraseSecreta = "";
        }

        while (true) {
            limparTela();
            mostrarCabecalho(usuario);
            System.out.println("\nTotal de acessos: " + usuario.numAcessos);

            System.out.println("\nCaminho do pasta: " + formularioPasta.caminhoPasta);
            System.out.println("Frase secreta: " + formularioPasta.fraseSecreta);

            if (formularioPasta.caminhoPasta.equals("")) {
                System.out.println("\nDigite o caminho da pasta ou SAIR para voltar:");
                formularioPasta.caminhoPasta = scanner.nextLine();
                if (formularioPasta.caminhoPasta.equalsIgnoreCase("sair")) {
                    formularioPasta.tipoRetorno = -1;
                    return formularioPasta;
                }
                continue;
            }
            if (formularioPasta.fraseSecreta.equals("")) {
                System.out.println("\nDigite a frase secreta ou SAIR para voltar:");
                formularioPasta.fraseSecreta = scanner.nextLine();
                if (formularioPasta.fraseSecreta.equalsIgnoreCase("sair")) {
                    formularioPasta.tipoRetorno = -1;
                    return formularioPasta;
                }
                continue;
            }


            if (!show) {
                if (!erro.equals("")) {
                    System.out.println("Erro: " + erro);
                }
                System.out.println("\nPressione ENTER para mostrar o conteúdo da pasta ou SAIR para voltar");
                opcao = scanner.nextLine().toLowerCase();
                if (opcao.equals("sair")) {
                    formularioPasta.tipoRetorno = -1;
                    return formularioPasta;
                } else if (opcao.equals("")){
                    formularioPasta.tipoRetorno = 0;
                    return formularioPasta;                   // RETURN ABRIR PASTA
                }
            } else {
                System.out.println("--- ARQUIVOS ---");
                for (int i = 0; i < linhas.size(); i++) {
                    LinhaIndice linha = linhas.get(i);
                    System.out.format("[%d] %s - %s (%s)\n", i + 1, linha.nomeArquivo, linha.usuario, linha.grupo);
                }
                System.out.println("----------------");

                if (!erro.equals("")) {
                    System.out.println("\n" + erro + "\n");
                }
                System.out.println("Digite o numero do arquivo para abrir ou SAIR para voltar");
                opcao = scanner.nextLine().toLowerCase();
                if (opcao.equals("sair")) {
                    formularioPasta.tipoRetorno = -1;
                    return formularioPasta;                  // RETURN SAIR
                } else {
                    try {
                        int index = Integer.parseInt(opcao);
                        if (index <= 0 || index > linhas.size()) {
                            erro = "OPCAO INVALIDA (NUMERO FORA DO INTERVALO)";
                        } else {
                            formularioPasta.tipoRetorno = index;
                            return formularioPasta;       // RETURN INDEX
                        }
                    } catch (Exception e) {
                        erro = "OPCAO INVALIDA (NAO EH UM NUMERO)";
                    }
                }
            }
        }
    }

    public static Operacao telaSaida(Usuario usuario) {
        int opcao;

        boolean isError = false;

        while (true) {
            limparTela();
            mostrarCabecalho(usuario);
            System.out.println("\nTotal de acessos: " + usuario.numAcessos);
            System.out.println("\nSaída do sistema:");

            System.out.println("\n1 - Encerrar Sessão");
            System.out.println("2 - Encerrar Sistema");
            System.out.println("3 - Cancelar");

            if (isError) {
                System.out.println("\nOPCAO INVALIDA\n");
            }

            Scanner scanner = new Scanner(System.in);
            opcao = scanner.nextInt();
            switch (opcao) {
                case 1:
                    return Operacao.ENCERRAR_SESSAO;
                case 2:
                    return Operacao.SAIR_SISTEMA;
                case 3:
                    return Operacao.RETORNAR_MENU;
                default:
                    isError = true;
                    break;
            }
        }
    }

    public static void mostrarRegistros(ArrayList<String> registros) {
        limparTela();
        System.out.println("=== REGISTROS DE ACESSO ===");
        for (String registro : registros) {
            System.out.println(registro);
        }
        System.out.println("===========================");
    }

    /** verifica se o email eh valido */
    public static String loginInicial(String erro) {
        String nome;
        final Pattern emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

        do {
            limparTela();
            System.out.println("===============================");
            if (erro != null) {
                System.out.println("\n(" + erro + ")\n");
            }

            System.out.println("Digite o seu nome de usuário: ");
            Scanner scanner = new Scanner(System.in);
            nome = scanner.nextLine();
            if (!emailPattern.matcher(nome).matches()) {
                erro = "O nome de usuário deve ser ume email válido!";
            }
        } while (!emailPattern.matcher(nome).matches());
        return nome;
    }
}

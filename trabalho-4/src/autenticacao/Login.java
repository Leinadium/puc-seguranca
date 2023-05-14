package autenticacao;

import basedados.Conexao;
import basedados.modelos.Usuario;
import registro.EnumRegistro;
import registro.Registrador;
import terminal.InterfaceTerminal;

public class Login {

    public static Usuario login(String erro) {
        Conexao conexao;
        Registrador registrador;
        try {
            registrador = Registrador.getInstance();
            conexao = Conexao.getInstance();
            registrador.setConexao(conexao);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            System.exit(1);
            return null;
        }


        // primeira autenticacao
        String nomeLogin;
        Usuario usuario = null;
        do {
            registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_1_INICIADA);
            nomeLogin = InterfaceTerminal.loginInicial(erro);
            try {
                usuario = conexao.getUsuario(nomeLogin);
            } catch (Exception e) {
                registrador.fazerRegistro(EnumRegistro.LOGIN_NAO_IDENTIFICADO);
                erro = "Usuario não existe no banco de dados";
                continue;
            }
            if (conexao.usuarioEstaBloqueado(usuario)) {
                registrador.fazerRegistro(EnumRegistro.LOGIN_IDENTIFICADO_BLOQUEADO, usuario.loginName);
                erro = "O acesso do usuário está bloquado";
                usuario = null;
                continue;
            } else {
                registrador.fazerRegistro(EnumRegistro.LOGIN_IDENTIFICADO_LIBERADO, usuario.loginName);
            }
            registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_1_ENCERRADA);

        } while (usuario == null);

        // segunda autenticacao
        registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_2_INICIADA);
        String senhaCheck = null;
        int tentativas_senha = 0;
        while (tentativas_senha < 3) {
            TecladoVirtual teclado = new TecladoVirtual();
            senhaCheck = teclado.lerSenha(usuario.senha, tentativas_senha);
            if (senhaCheck != null) {
                registrador.fazerRegistro(EnumRegistro.SENHA_VERIFICADA);
                System.out.println("Senha correta!");
                tentativas_senha = 0;
                break;
            } else {
                System.out.println("Senha incorreta!");
                tentativas_senha++;

                if (tentativas_senha == 1) { registrador.fazerRegistro(EnumRegistro.SENHA_INVALIDA_1, usuario.loginName); }
                else if (tentativas_senha == 2) { registrador.fazerRegistro(EnumRegistro.SENHA_INVALIDA_2, usuario.loginName); }
                else  { registrador.fazerRegistro(EnumRegistro.SENHA_INVALIDA_3, usuario.loginName); }
            }
        }
        registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_2_ENCERRADA);
        if (tentativas_senha >= 3) {
            System.out.println("O acesso do usuário foi bloqueado.");
            conexao.bloquearUsuario(usuario);
            registrador.fazerRegistro(EnumRegistro.ACESSO_BLOQUEADO_2, usuario.loginName);
            return null;
        }

        // terceira autenticacao
        try {
            ArquivoTexto arquivo = new ArquivoTexto(usuario.senha, usuario.semente);
            arquivo.criaArquivo();
        } catch (Exception e) {
            System.out.println("Erro ao criar arquivo token.txt");
            System.exit(1);
        }

        registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_3_INICIADA);
        boolean tokenCheck;
        int tentativas_token = 0;
        while (tentativas_token < 3) {
            tokenCheck = VerificadorToken.verifica(usuario, senhaCheck);
            if (tokenCheck) {
                registrador.fazerRegistro(EnumRegistro.TOKEN_VERIFICADO);
                System.out.println("Token correto!");
                tentativas_token = 0;
                break;
            } else{
                System.out.println("Token incorreto!");
                tentativas_token++;

                if (tentativas_token == 1) { registrador.fazerRegistro(EnumRegistro.TOKEN_INVALIDO_1, usuario.loginName); }
                else if (tentativas_token == 2) { registrador.fazerRegistro(EnumRegistro.TOKEN_INVALIDO_2, usuario.loginName); }
                else  { registrador.fazerRegistro(EnumRegistro.TOKEN_INVALIDO_3, usuario.loginName); }

                System.out.println("Tentativas restantes: " + (3 - tentativas_token));
            }
        }
        registrador.fazerRegistro(EnumRegistro.AUTENTICACAO_3_ENCERRADA);

        if (tentativas_token == 3) {
            System.out.println("O acesso do usuário foi bloqueado.");
            conexao.bloquearUsuario(usuario);
            registrador.fazerRegistro(EnumRegistro.ACESSO_BLOQUEADO_3, usuario.loginName);
            return null;
        }
        return usuario;
    }
}

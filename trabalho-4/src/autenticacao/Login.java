package autenticacao;

import basedados.Conexao;
import basedados.modelos.Usuario;
import terminal.InterfaceTerminal;

public class Login {

    public static Usuario login() {
        Conexao conexao;
        try {
            conexao = Conexao.getInstance();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            System.exit(1);
            return null;
        }

        // primeira autenticacao
        String nomeLogin;
        Usuario usuario = null;
        String erro = null;
        do {
            nomeLogin = InterfaceTerminal.loginInicial(erro);
            try {
                usuario = conexao.getUsuario(nomeLogin);
            } catch (Exception e) {
                erro = "Usuario não existe no banco de dados";
                continue;
            }

            if (conexao.usuarioEstaBloqueado(usuario)) {
                erro = "O acesso do usuário está bloquado";
            }

        } while (usuario == null);


        // segunda autenticacao
        boolean senhaCheck;
        int tentativas_senha = 0;
        while (tentativas_senha < 3) {
            TecladoVirtual teclado = new TecladoVirtual();
            senhaCheck = teclado.lerSenha(usuario.senha);
            if (senhaCheck) {
                System.out.println("Senha correta!");
                tentativas_senha = 0;
                break;
            } else {
                System.out.println("Senha incorreta!");
                tentativas_senha++;
                System.out.println("Tentativas restantes: " + (3 - tentativas_senha));
            }
        }
        if (tentativas_senha == 3) {
            System.out.println("O acesso do usuário foi bloqueado.");
            conexao.bloquearUsuario(usuario);
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

        boolean tokenCheck;
        int tentativas_token = 0;
        while (tentativas_token < 3) {
            tokenCheck = VerificadorToken.verifica(usuario);
            if (tokenCheck) {
                System.out.println("Token correto!");
                tentativas_token = 0;
                break;
            } else{
                System.out.println("Token incorreto!");
                tentativas_token++;
                System.out.println("Tentativas restantes: " + (3 - tentativas_token));
            }
        }

        if (tentativas_token == 3) {
            System.out.println("O acesso do usuário foi bloqueado.");
            conexao.bloquearUsuario(usuario);
            return null;
        }
        return usuario;
    }
}

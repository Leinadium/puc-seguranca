import basedados.Usuario;
import diretorio.Diretorio;
import diretorio.Restaurador;
import basedados.Conexao;
import terminal.FormularioCadastro;
import terminal.InterfaceTerminal;
import terminal.Operacao;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CofreDigital {

    Diretorio diretorio;
    Usuario usuario;
    Conexao conexao;

    public static void main(String[] args) throws Exception {
        // Conexao conexao = Conexao.getInstance();

        Usuario u = new Usuario();
        u.login = "leinadium";
        u.grupo = "admin";
        u.nome = "Daniel";
        InterfaceTerminal.mostrarFormularioCadastro(u);


        Diretorio dir = new Diretorio("/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Files");

        String pathPk = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-pkcs8-des.key";
        String pathCert = "/home/leinadium/puc/puc-seguranca/trabalho-4/Pacote-T4/Keys/admin-x509.crt";

        PrivateKey privateKey = Restaurador.restauraChavePrivada(pathPk, "admin");
        PublicKey publicKey = Restaurador.restauraChavePublica(pathCert);
        dir.init(privateKey, publicKey);
        dir.show();
    }

    public static void mainFinal(String dirPath) {
        CofreDigital cofreDigital = new CofreDigital();
        cofreDigital.diretorio = new Diretorio(dirPath);
        try {
            cofreDigital.conexao = Conexao.getInstance();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados.");
            return;
        }

        // TODO: primeiro cadastro do admin

        while (true) {
            // espera realizar uma autenticacao bem sucedida
            cofreDigital.loopAutenticacao();

            // loop principal do sistema
            cofreDigital.loopSistema();
        }

    }

    private void loopAutenticacao() {
        // TODO: autenticacao do usuario
        this.usuario = new Usuario();
        this.usuario.login = "leinadium";
        this.usuario.grupo = "admin";
        this.usuario.nome = "Daniel";
    }

    private void loopSistema() {
        while (true) {
            Operacao operacao = InterfaceTerminal.menuPrincipal(this.usuario);
            switch (operacao) {
                case CADASTRAR_NOVO_USUARIO:
                    this.cadastrarNovoUsuario();
                    break;
                case CONSULTAR_PASTA:
                    this.consultarPasta();
                    break;
                case SAIR_SISTEMA:
                    return;
            }
        }
    }

    private void cadastrarNovoUsuario() {
        FormularioCadastro formularioCadastro = InterfaceTerminal.mostrarFormularioCadastro(this.usuario);
        if (formularioCadastro == null) {
            return;
        }
        // TODO: cadastrar novo usuario no banco
    }

    private void consultarPasta() {

    }
}

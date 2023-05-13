import autenticacao.ArquivoTexto;
import autenticacao.GeradorToken;
import autenticacao.TecladoVirtual;
import terminal.InterfaceTerminal;

import java.util.Date;

public class AiToken {
    // TECLADO VIRTUAL (PEGA A SENHA DO CARA)
    // ME DEVOLVE UM TOKEN REFERENTE A HORA ATUAL

    public static void main(String[] args) throws Exception {
        ArquivoTexto at = ArquivoTexto.recuperaArquivo();

        boolean senhaCorreta = false;
        while (!senhaCorreta) {
            TecladoVirtual tecladoVirtual = new TecladoVirtual();
            senhaCorreta = tecladoVirtual.lerSenha(at.senhaBCrypt, -1);
        }

        long minutoAtual = new Date().getTime() / 60000;
        int token = GeradorToken.geraToken(at.semente, minutoAtual);

        InterfaceTerminal.limparTela();
        System.out.println("====================================");
        System.out.println("\niToken: " + String.format("%06d", token));
    }
}

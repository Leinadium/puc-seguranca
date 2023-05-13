import autenticacao.ArquivoTexto;
import autenticacao.GeradorToken;
import autenticacao.TecladoVirtual;

import java.util.Date;

public class aiToken {
    // TECLADO VIRTUAL (PEGA A SENHA DO CARA)
    // ME DEVOLVE UM TOKEN REFERENTE A HORA ATUAL

    public static void main(String[] args) throws Exception {
        ArquivoTexto at = ArquivoTexto.recuperaArquivo();

        TecladoVirtual tecladoVirtual = new TecladoVirtual();
        tecladoVirtual.lerSenha(at.senhaBCrypt);

        long minutoAtual = new Date().getTime() / 60000;
        int token = GeradorToken.geraToken(at.semente, minutoAtual);

        System.out.println("\niToken: " + token);
    }
}

package autenticacao;

import basedados.modelos.Usuario;

import java.util.Scanner;
import java.util.Date;
public class VerificadorToken {

    public static boolean verifica(Usuario usuario) {

        long tempoAtual = new Date().getTime()/ (60000);
        byte[] semente = usuario.semente;
        System.out.print("Token: " + GeradorToken.geraToken(semente, tempoAtual) + "\n");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o token do usuário: ");
        int tokenUsuario = scanner.nextInt();
        scanner.close();

        boolean ehValido = verificaToken(semente, tokenUsuario);

        return ehValido;
    }

    private static boolean verificaToken(byte[] semente, int tokenUsuario) {
        long tempoAtual = new Date().getTime();
        long minutoAtual = tempoAtual / 60000;
        long minutoAnterior = minutoAtual - 1;
        long minutoProximo = minutoAtual + 1;
        int[] tokensEsperados = { GeradorToken.geraToken(semente,minutoAnterior), GeradorToken.geraToken(semente,minutoAtual), GeradorToken.geraToken(semente,minutoProximo) };
        System.out.print("Token esperado: " + tokensEsperados[0] + "\n Token esperado: " + tokensEsperados[1] + "\n Token esperado: " + tokensEsperados[2] + "\n");
        for (int tokenEsperado : tokensEsperados) {
            if (tokenEsperado == tokenUsuario) {
                return true;
            }
        }
        return false;
    }

}
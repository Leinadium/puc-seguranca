import basedados.Conexao;
import terminal.InterfaceTerminal;

import java.util.ArrayList;

public class LogViewer {
    public static void main(String[] args) {
        Conexao conexao;
        try {
            conexao = Conexao.getInstance();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o banco de dados: " + e.getMessage());
            return;
        }
        ArrayList<String> registros = conexao.getTodosRegistros();
        InterfaceTerminal.mostrarRegistros(registros);
    }
}

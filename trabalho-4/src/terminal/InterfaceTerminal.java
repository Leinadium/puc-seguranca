package terminal;

public class InterfaceTerminal {
    public static void mostrarInicio() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("Bem vindo ao Cofre Digital!");
        System.out.println("Escolha uma opção:");
        System.out.println("1 - Criar novo diretório");
        System.out.println("2 - Abrir diretório existente");
        System.out.println("3 - Sair");
    }
}

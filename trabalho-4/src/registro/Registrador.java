package registro;

/** Implementacao de um logger. */
public class Registrador {
    private static Registrador instance;
    Registrador() {}

    /** Retorna a instancia do logger. */
    public static Registrador getInstance() {
        if (instance == null) {
            instance = new Registrador();
        }
        return instance;
    }

    public void fazerRegistro() {

    }
}

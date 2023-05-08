package basedados;

import java.sql.*;


public class Conexao {
    private static final String URL = "jdbc:sqlite:./banco.db";
    private static Conexao instance;
    Connection conn;
    public static Conexao getInstance() throws SQLException {
        if (instance == null) {
            instance = new Conexao();
            instance.conn = DriverManager.getConnection(URL);
        }
        return instance;
    }
}

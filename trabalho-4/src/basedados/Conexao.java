package basedados;

import basedados.modelos.Chaveiro;
import basedados.modelos.Grupo;
import basedados.modelos.Usuario;

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

    static final String createUsuario = "CREATE TABLE IF NOT EXISTS usuarios (\n"
            + "	uid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "	loginName TEXT NOT NULL UNIQUE,\n"
            + "	nome TEXT NOT NULL,\n"
            + "	numAcessos INTEGER,\n"
            + "	bloqueado INTEGER NOT NULL,\n"
            + "	fraseSecreta TEXT NOT NULL,\n"
            + "	senha BLOB NOT NULL,\n"
            + "	semente TEXT NOT NULL,\n"
            + "	kid INTEGER NOT NULL,\n"
            + "	gid INTEGER NOT NULL\n"
            + ");";

    static final String createChaveiro = "CREATE TABLE IF NOT EXISTS chaveiros (\n"
            + "   kid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "   chavePublicaPem TEXT NOT NULL,\n"
            + "   chavePrivada BLOB NOT NULL\n"
            + ");";

    static final String createGrupo = "CREATE TABLE IF NOT EXISTS grupos (\n"
            + "   gid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "   nome TEXT NOT NULL\n"
            + ");";

    static final String createMensagem = "CREATE TABLE IF NOT EXISTS mensagens (\n"
            + " mid INTEGER PRIMARY KEY,\n"
            + " texto TEXT NOT NULL\n"
            + ");";

    static final String createRegistro = "CREATE TABLE IF NOT EXISTS registros (\n"
            + "   uid INTEGER NOT NULL,\n"
            + "   mid INTEGER NOT NULL,\n"
            + "   info1 TEXT, \n"
            + "   info2 TEXT, \n"
            + "   info3 TEXT \n"
            + ");";

    public void criar() {
        // cria as tabelas no banco
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(createUsuario);
            stmt.execute(createChaveiro);
            stmt.execute(createGrupo);
            stmt.execute(createMensagem);
            stmt.execute(createRegistro);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Grupo getGrupo(int gid) throws Exception {
        Grupo grupo = new Grupo();
        String sql = "SELECT gid, nome FROM grupos WHERE gid = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, gid);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            grupo.gid = rs.getInt("gid");
            grupo.nome = rs.getString("nome");
        } else {
            throw new Exception(String.format("Grupo %d não encontrado", gid));
        }
        return grupo;
    }

    /** pega um grupo pelo nome, ou cria um novo */
    public Grupo getGrupo(String nome) throws Exception {
        // primeiro, tenta pegar o grupo com esse nome
        String sql = "SELECT gid, nome FROM grupos WHERE nome = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nome);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            // se encontrou, retorna o grupo
            Grupo grupo = new Grupo();
            grupo.gid = rs.getInt("gid");
            grupo.nome = rs.getString("nome");
            return grupo;
        } else {
            // se não encontrou, cria um novo grupo
            sql = "INSERT INTO grupos (nome) VALUES (?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nome);
            pstmt.executeUpdate();

            // pega o gid do grupo que acabou de ser criado
            sql = "SELECT gid, nome FROM grupos WHERE nome = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nome);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // se encontrou, retorna o grupo
                Grupo grupo = new Grupo();
                grupo.gid = rs.getInt("gid");
                grupo.nome = rs.getString("nome");
                return grupo;
            } else {
                throw new Exception(String.format("Grupo %s não encontrado", nome));
            }
        }
    }

    public Chaveiro getChaveiro(int kid) throws Exception {
        Chaveiro chaveiro = new Chaveiro();
        String sql = "SELECT kid, chavePublicaPem, chavePrivada FROM chaveiros WHERE kid = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, kid);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            chaveiro.kid = rs.getInt("kid");
            chaveiro.chavePublicaPem = rs.getString("chavePublicaPem");
            chaveiro.chavePrivadaBytes = rs.getBytes("chavePrivada");
        } else {
            throw new Exception(String.format("Chaveiro %d não encontrado", kid));
        }
        return chaveiro;
    }

    public Usuario getUsuario(String loginName) throws Exception {
        Usuario usuario = new Usuario();
        String sql = "SELECT uid, loginName, nome, numAcessos, bloqueado, fraseSecreta, senha, semente, kid, gid " +
                     "FROM usuarios WHERE loginName = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, loginName);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            usuario.uid = rs.getInt("uid");
            usuario.loginName = rs.getString("loginName");
            usuario.nome = rs.getString("nome");
            usuario.numAcessos = rs.getInt("numAcessos");
            usuario.bloqueado = rs.getInt("bloqueado");
            usuario.fraseSecreta = rs.getString("fraseSecreta");
            usuario.senha = rs.getBytes("senha");
            usuario.semente = rs.getString("semente");
            usuario.chaveiro = getChaveiro(rs.getInt("kid"));
            usuario.grupo = getGrupo(rs.getInt("gid"));
        } else {
            throw new Exception(String.format("Usuário %s não encontrado", loginName));
        }
        return usuario;
    }

    /** Guarda o usuario no banco */
    public void setUsuario(Usuario usuario) throws Exception {
        // primeiro, cria o chaveiro
        String sql = "INSERT INTO chaveiros (chavePublicaPem, chavePrivada) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, usuario.chaveiro.chavePublicaPem);
        pstmt.setBytes(2, usuario.chaveiro.chavePrivadaBytes);
        pstmt.executeUpdate();
        // pega o id do chaveiro
        sql = "SELECT kid FROM chaveiros WHERE chavePublicaPem = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, usuario.chaveiro.chavePublicaPem);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            usuario.chaveiro.kid = rs.getInt("kid");
        } else {
            throw new Exception("Chaveiro recem criado não encontrado");
        }

        // cria o usuario
        String sql2 = "INSERT INTO usuarios (" +
                "loginName, nome, numAcessos, bloqueado, fraseSecreta, senha, semente, kid, gid) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        pstmt = conn.prepareStatement(sql2);
        pstmt.setString(1, usuario.loginName);
        pstmt.setString(2, usuario.nome);
        pstmt.setInt(3, usuario.numAcessos);
        pstmt.setInt(4, usuario.bloqueado);
        pstmt.setString(5, usuario.fraseSecreta);
        pstmt.setBytes(6, usuario.senha);
        pstmt.setString(7, usuario.semente);
        pstmt.setInt(8, usuario.chaveiro.kid);
        pstmt.setInt(9, usuario.grupo.gid);
        pstmt.executeUpdate();

    }

    /** Coleta o chaveiro de algum admin do banco */
    public Chaveiro chaveiroAdmin() {
        String sql = "SELECT c.kid, c.certificado, c.chavePrivada " +
                     "FROM chaveiros c, usuarios u, grupos g " +
                     "WHERE c.kid = u.kid AND u.gid = g.gid AND g.nome = 'administrador'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Chaveiro chaveiro = new Chaveiro();
                chaveiro.kid = rs.getInt("kid");
                chaveiro.chavePublicaPem = rs.getString("chavePublicaPem");
                chaveiro.chavePrivadaBytes = rs.getBytes("chavePrivada");
                return chaveiro;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}

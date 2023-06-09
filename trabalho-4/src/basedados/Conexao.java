package basedados;

import basedados.modelos.Chaveiro;
import basedados.modelos.Grupo;
import basedados.modelos.Usuario;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


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

    private Conexao() {}

    static final String createUsuario = "CREATE TABLE IF NOT EXISTS usuarios (\n"
            + "	uid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "	loginName TEXT NOT NULL UNIQUE,\n"
            + "	nome TEXT NOT NULL,\n"
            + "	numAcessos INTEGER,\n"
            + "	bloqueado TEXT,\n"
            + "	senha BLOB NOT NULL,\n"
            + "	semente BLOB NOT NULL,\n"
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
            + "   rid INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "   mid INTEGER NOT NULL,\n"
            + "   quando TEXT,\n"
            + "   info1 TEXT,\n"
            + "   info2 TEXT\n"
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
            this.preencheMensagens();
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

    public boolean existeLoginName(String loginName) throws Exception {
        String sql = "SELECT uid FROM usuarios WHERE loginName = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, loginName);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    public Usuario getUsuario(String loginName) throws Exception {
        Usuario usuario = new Usuario();
        String sql = "SELECT uid, loginName, nome, numAcessos, bloqueado, senha, semente, kid, gid " +
                     "FROM usuarios WHERE loginName = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, loginName);
        ResultSet rs = pstmt.executeQuery();



        if (rs.next()) {
            // pega a data de bloqueio de rs.getLong(bloqueado)
            java.util.Date bloqueado = new java.util.Date(rs.getLong("bloqueado"));

            usuario.uid = rs.getInt("uid");
            usuario.loginName = rs.getString("loginName");
            usuario.nome = rs.getString("nome");
            usuario.numAcessos = rs.getInt("numAcessos");
            usuario.bloqueado = bloqueado;
            usuario.senha = rs.getString("senha");
            usuario.semente = rs.getBytes("semente");
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
                "loginName, nome, numAcessos, bloqueado, senha, semente, kid, gid) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        pstmt = conn.prepareStatement(sql2);
        pstmt.setString(1, usuario.loginName);
        pstmt.setString(2, usuario.nome);
        pstmt.setInt(3, usuario.numAcessos);
        pstmt.setDate(4, (Date) usuario.bloqueado);

        pstmt.setString(5, usuario.senha);
        pstmt.setBytes(6, usuario.semente);
        pstmt.setInt(7, usuario.chaveiro.kid);
        pstmt.setInt(8, usuario.grupo.gid);
        pstmt.executeUpdate();
    }

    /** Coleta o chaveiro de algum admin do banco */
    public Chaveiro chaveiroAdmin() {
        String sql = "SELECT c.kid, c.chavePublicaPem, c.chavePrivada " +
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

    private void setMensagem(int mid, String texto) throws SQLException {
        String sql = "INSERT INTO mensagens (mid, texto) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, mid);
        pstmt.setString(2, texto);
        pstmt.executeUpdate();
    }

    private void preencheMensagens() throws SQLException {
        // primeiro, verifica se tem alguma mensagem ja no banco. se tiver, retorna logo
        String sql = "SELECT mid FROM mensagens";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) { return; }
        setMensagem(1001, "Sistema iniciado");
        setMensagem(1002, "Sistema encerrado");
        setMensagem(1003, "Sessão iniciada para %1$s");
        setMensagem(1004, "Sessão encerrada para %1$s");
        setMensagem(2001, "Autenticação etapa 1 iniciada");
        setMensagem(2002, "Autenticação etapa 1 encerrada");
        setMensagem(2003, "Login name %1$s identificado com acesso liberado");
        setMensagem(2004, "Login name %1$s identificado com acesso bloqueado");
        setMensagem(2005, "Login name %1$s não identificado");
        setMensagem(3001, "Autenticação etapa 2 iniciada para %1$s");
        setMensagem(3002, "Autenticação etapa 2 encerrada para %1$s");
        setMensagem(3003, "Senha pessoal verificada positivamente para %1$s");
        setMensagem(3004, "Primeiro erro da senha pessoal contabilizado para %1$s");
        setMensagem(3005, "Segundo erro da senha pessoal contabilizado para %1$s");
        setMensagem(3006, "Terceiro erro da senha pessoal contabilizado para %1$s");
        setMensagem(3007, "Acesso do usuario %1$s bloqueado pela autenticação etapa 2");
        setMensagem(4001, "Autenticação etapa 3 iniciada para %1$s");
        setMensagem(4002, "Autenticação etapa 3 encerrada para %1$s");
        setMensagem(4003, "Token verificado positivamente para %1$s");
        setMensagem(4004, "Primeiro erro de token contabilizado para %1$s");
        setMensagem(4005, "Segundo erro de token contabilizado para %1$s");
        setMensagem(4006, "Terceiro erro de token contabilizado para %1$s");
        setMensagem(4007, "Acesso do usuario %1$s bloqueado pela autenticação etapa 3");
        setMensagem(5001, "Tela principal apresentada para %1$s");
        setMensagem(5002, "Opção 1 do menu selecionada por %1$s");
        setMensagem(5003, "Opção 2 do menu selecionada por %1$s");
        setMensagem(5004, "Opção 3 do menu selecionada por %1$s");
        setMensagem(6001, "Tela de cadastro apresentada para %1$s");
        setMensagem(6002, "Botão cadastrar pressionado por %1$s");
        setMensagem(6003, "Senha pessoal inválida fornecida por %1$s");
        setMensagem(6004, "Caminho do certificado digital inválido fornecido por %1$s");
        setMensagem(6005, "Chave privada verificada negativamente para %1$s (caminho inválido)");
        setMensagem(6006, "Chave privada verificada negativamente para %1$s (frase secreta inválida)");
        setMensagem(6007 ,"Chave privada verificada negativamente para %1$s (assinatura digital inválida)");
        setMensagem(6008, "Confirmação de dados aceita por %1$s");
        setMensagem(6009, "Confirmação de dados rejeitada por %1$s");
        setMensagem(6010, "Botão voltar de cadastro para o menu principal pressionado por %1$s");
        setMensagem(7001, "Tela de consulta de arquivos secretos apresentada por %1$s");
        setMensagem(7002, "Botão voltar de consulta para o menu principal pressionado por %1$s");
        setMensagem(7003, "Botão Listar de consulta pressionado por %1$s");
        setMensagem(7004, "Caminho de pasta inválido fornecido por %1$s");
        setMensagem(7005, "Arquivo de índice decriptado com sucesso para %1$s");
        setMensagem(7006, "Arquivo de índice verificado (integridade e autenticidade) com sucesso para %1$s");
        setMensagem(7007, "Falha na decriptação do arquivo de índice para %1$s");
        setMensagem(7008, "Falha na verificação (integridade e autenticidade) do arquivo de índice para %1$s");
        setMensagem(7009, "Lista de arquivos presentes no índice apresentada para %1$s");
        setMensagem(7010, "Arquivo %2$s selecionado por %1$s para decriptação");
        setMensagem(7011, "Acesso permitido ao arquivo %2$s para %1$s");
        setMensagem(7012, "Acesso negado ao arquivo %2$s para %1$s");
        setMensagem(7013, "Arquivo %2$s decriptado com sucesso para %1$s");
        setMensagem(7014, "Arquivo %2$s verificado (integridade e autenticidade) com sucesso para %1$s");
        setMensagem(7015, "Falha na decriptação do arquivo %s para %1$s");
        setMensagem(7016, "Falha na verificação (integridade e autenticada) do arquivo %2$s para %1$s");
        setMensagem(8001, "Tela de saída apresentada por %1$s");
        setMensagem(8002, "Botão encerrar sessão pressionado por %1$s");
        setMensagem(8003, "Botão encerrar sistema pressionado por %1$s");
        setMensagem(8004, "Botão voltar de sair para o menu principal presionado por %1$s");
    }

    public void setRegistro(int codigo, String nome, String arquivo) {
        String sql = "INSERT INTO registros (mid, quando, info1, info2) VALUES (?, ?, ?, ?)";
        try {
            // https://stackoverflow.com/questions/46712635/what-is-the-correct-way-to-insert-datetimes-into-sqlite-from-java
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String ts = sdf.format(timestamp);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, codigo);
            pstmt.setString(2, ts);
            pstmt.setString(3, nome);
            pstmt.setString(4, arquivo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao guardar registro: " + e.getMessage());
        }
    }

    public ArrayList<String> getTodosRegistros() {
        // coleta todos os registros, ordenando pelo campo "quando"
        String sql = "SELECT r.rid, m.texto, r.quando, r.info1, r.info2 " +
                     "FROM registros r, mensagens m " +
                     "WHERE m.mid = r.mid " +
                     "ORDER BY quando DESC";

        ArrayList<String> ret = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int rid = rs.getInt("rid");
                String texto = rs.getString("texto");
                String quando = rs.getString("quando");
                String info1 = rs.getString("info1");
                String info2 = rs.getString("info2");

                String textoPreenchido = String.format(texto, info1, info2);
                String s = String.format("(id:%d)[%s] %s", rid, quando, textoPreenchido);
                ret.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    public boolean usuarioEstaBloqueado(Usuario usuario) {
        if (usuario.bloqueado == null) {
            return false;
        }
        // verificando se a hora atual eh dois minutos maior que a hora em .bloqueado
        Calendar cal = Calendar.getInstance();
        cal.setTime(usuario.bloqueado);
        cal.add(Calendar.MINUTE, 2);
        java.util.Date data = cal.getTime();

        return data.after(new java.util.Date());
    }

    public void bloquearUsuario(Usuario usuario) {
        String sql = "UPDATE usuarios SET bloqueado = ? WHERE uid = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // salva a hora atual
            pstmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
            pstmt.setInt(2, usuario.uid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int quantidadeUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public void atualizarNumeroAcessos(Usuario usuario) {
        String sql = "UPDATE usuarios SET numAcessos = ? WHERE uid = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, usuario.numAcessos + 1);
            pstmt.setInt(2, usuario.uid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

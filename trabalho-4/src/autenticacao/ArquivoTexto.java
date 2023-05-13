package autenticacao;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Base64;

public class ArquivoTexto {
    /**
     * Cria um arquivo token.txt com a senha e a semente.
     * Na primeira linha, tem a senha bcrypt.
     * Na segunda linha, a semente codificada em base64.
     */

    public String senhaBCrypt;
    public byte[] semente;

    public ArquivoTexto (String senhaBCrypt, byte[] semente) {
        this.senhaBCrypt = senhaBCrypt;
        this.semente = semente;
    }

    public void criaArquivo() {
        // codifica a semente em base64
        String semente64 = Base64.getEncoder().encodeToString(semente);

        // cria o arquivo
        try {
            PrintWriter writer = new PrintWriter("token.txt", "UTF-8");
            writer.println(senhaBCrypt);
            writer.println(semente64);
            writer.close();
        } catch (Exception e) {
            System.out.println("Erro ao criar arquivo token.txt");
        }
    }

    public static ArquivoTexto recuperaArquivo() throws Exception {
        BufferedReader reader;
        reader = new BufferedReader(new java.io.FileReader("token.txt"));
        String senhaBCrypt = reader.readLine();
        String semente64 = reader.readLine();
        reader.close();

        // decodifica a semente em base64
        byte[] semente = Base64.getDecoder().decode(semente64);

        return new ArquivoTexto(senhaBCrypt, semente);

    }

}

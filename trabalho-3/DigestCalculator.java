/*
 * Daniel Guimarães     - 1910462
 * Luiz Fellipe Augusto - 1711256
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class DigestCalculator {

    Arquivo arquivo;    // informacoes do arquivo XML
    Pasta pasta;        // informacoes da pasta
    public static String toHex(byte[] bytes) {
        // converte o signature para hexadecimal
        StringBuilder buf = new StringBuilder();
        for (byte meusByte : bytes) {
            String hex = Integer.toHexString(0x0100 + (meusByte & 0x00FF)).substring(1);
            buf.append(hex.length() < 2 ? "0" : "").append(hex);
        }
        return buf.toString();
    }

    public int run(String[] args) {
        // validando args
        if (args.length != 3) {
            System.err.println("Numero de argumentos invalido");
            System.err.println("Uso: java DigestCalculator <TipoDigest> <CaminhoPastaArquivos> <CaminhoArqListaDigest>");
            return 1;
        }
        // tipo do digest so pode ser algum dentre os 4 definidos
        // (copilot recomenda utilizar .equals() em vez de == para comparar strings)
        String tipoDigest = args[0];
        if (!Objects.equals(tipoDigest, "MD5") &&
                !Objects.equals(tipoDigest, "SHA1") &&
                !Objects.equals(tipoDigest, "SHA256") &&
                !Objects.equals(tipoDigest, "SHA512")) {
            System.err.println("Tipo de digest invalido. Tipos validos: MD5, SHA1, SHA256, SHA512");
            return 1;
        }

        String caminhoPastaArquivos = args[1];
        String caminhoArqListaDigest = args[2];

        // criando o objeto Arquivo
        try {
            this.arquivo = new Arquivo(caminhoArqListaDigest);
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo XML " + caminhoArqListaDigest + ": " + e.getMessage());
            return 1;
        }

        // criando o objeto Pasta
        try {
            this.pasta = new Pasta(caminhoPastaArquivos, tipoDigest);
        } catch (Exception e) {
            System.err.println("Erro ao ler a pasta " + caminhoPastaArquivos + ": " + e.getMessage());
            return 1;
        }

        // hashmap para armazenar o status dos arquivos encontrados
        HashMap<String, String> statusArquivos = new HashMap<>();
        // hashmap para armazenar os hashes encontrados e seus respectivos filenames
        HashMap<String, String> digestsEncontrados = new HashMap<>();
        // hashmap para armazenar os novos hashes encontrados
        HashMap<String, String> novosDigests = new HashMap<>();

        // iterando sobre todos os arquivos da pasta
        for (String filename : this.pasta.digests.keySet()) {
            // primeiro, verifica se o digest do arquivo ja foi encontrado
            String dig = this.pasta.digests.get(filename);
            if (digestsEncontrados.containsKey(dig)) {
                // marco como colisao
                statusArquivos.put(filename, dig + " COLISION");
                // atualiza o status do arquivo que ja tinha o mesmo digest
                String filenameColisao = digestsEncontrados.get(dig);
                statusArquivos.put(filenameColisao, dig + " COLISION");
                // se este arquivo ja foi adicionado como novoDigest, remove (para nao inserir no xml)
                novosDigests.remove(filenameColisao);

            } else {
                // verifica se esse arquivo esta presente no arquivo xml
                if (this.arquivo.entries.containsKey(filename)) {
                    // verificando se eu tenho o digest para o meu tipo de digest
                    HashMap<String, String> digestsDisponiveis = this.arquivo.entries.get(filename);
                    if (digestsDisponiveis.containsKey(tipoDigest)) {
                        // comparando digests
                        if (Objects.equals(dig, digestsDisponiveis.get(tipoDigest))) {
                            statusArquivos.put(filename, dig + " OK");
                        } else {
                            statusArquivos.put(filename, dig + " NOT_OK");
                        }
                    } else {
                        statusArquivos.put(filename, dig + " NOT_FOUND");
                        novosDigests.put(filename, dig);    // atualizando o arquivo
                    }
                } else {
                    statusArquivos.put(filename, dig + " NOT_FOUND");
                    novosDigests.put(filename, dig);    // atualizando o arquivo
                }

                // atualizando digestsEncontrados
                digestsEncontrados.put(dig, filename);
            }
        }

        // Exibindo status dos arquivos
        for (String filename : statusArquivos.keySet()) {
            System.out.println(filename + " " + tipoDigest + " " + statusArquivos.get(filename));
        }

        // atualizando o arquivo
        try {
            for (String filename : novosDigests.keySet()) {
                this.arquivo.adicionaEntry(filename, tipoDigest, novosDigests.get(filename));
            }


            this.arquivo.atualizar();
        } catch (Exception e) {
            System.err.println("Erro ao atualizar o arquivo XML " + caminhoArqListaDigest + ": " + e.getMessage());
            return 1;
        }

        return 0;
    }

    /** Executa o loop principal do programa
     *
     * @param args: <TipoDigest> <CaminhoPastaArquivos> <CaminhoArqListaDigest>
     */
    public static void main (String[] args) {
        DigestCalculator dc = new DigestCalculator();
        System.exit(dc.run(args));
    }
}

class Pasta {
    public String caminho;
    public String tipoDigest;
    public HashMap<String, String> digests;

    public Pasta(String caminho, String tipoDigest) throws Exception {
        this.caminho = caminho;
        this.tipoDigest = tipoDigest;
        this.digests = new HashMap<>();

        MessageDigest md = MessageDigest.getInstance(tipoDigest);
        // pegando todos os arquivos presentes na pasta definido por caminho
        // https://www.baeldung.com/java-list-directory-files
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(caminho))) {
            for (Path path: stream) {
                if (Files.isDirectory(path)) continue;  // pula se for uma pasta

                String filename = path.getFileName().toString();
                // fazendo o digest do arquivo
                // fazendo por batch
                try (InputStream is = Files.newInputStream(path)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) > 0) {
                        md.update(buffer, 0, read);
                    }
                }
                // pegando o digest
                String dig = DigestCalculator.toHex(md.digest());
                // adicionando o digest ao hashmap
                this.digests.put(filename, dig);
            }
        }
    }
}

class Arquivo {
    public String caminho;
    public HashMap<String, HashMap<String, String>> entries;

    public Arquivo(String caminho) throws Exception {
        this.caminho = caminho;
        this.entries = new HashMap<>();

        // fazendo parsing do arquivo XML
        // https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(caminho));
        doc.getDocumentElement().normalize();

        // TODO: confirmar validação do XML de acordo com o enunciado
        // iterando sobre cada tag <FILE_ENTRY> dentro da tag raiz <CATALOG>
        NodeList nodesEntry = doc.getElementsByTagName("FILE_ENTRY");
        for (int i = 0; i < nodesEntry.getLength(); i ++) {
            // validando o node
            Node node = nodesEntry.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;  // pula se nao for um elemento
            Element element = (Element) node;

            // pega o nome do arquivo
            String nomeArquivo = element.getElementsByTagName("FILE_NAME").item(0).getTextContent();
            HashMap<String, String> novoEntry = new HashMap<>();

            // pega os digests
            NodeList nodesDigest = element.getElementsByTagName("DIGEST_ENTRY");
            for (int j = 0; j < nodesDigest.getLength(); j ++) {
                // validando o node
                Node nodeDigestEntry = nodesDigest.item(j);
                if (nodeDigestEntry.getNodeType() != Node.ELEMENT_NODE) continue;  // pula se nao for um elemento
                Element elementDigestEntry = (Element) nodeDigestEntry;

                // pega o tipo e hex do digest
                String tipoDigest = elementDigestEntry.getElementsByTagName("DIGEST_TYPE").item(0).getTextContent();
                String hexDigest = elementDigestEntry.getElementsByTagName("DIGEST_HEX").item(0).getTextContent();

                // atualiza o entry
                novoEntry.put(tipoDigest, hexDigest);
            }
            // adiciona o entry no hashmap
            this.entries.put(nomeArquivo, novoEntry);
        }
    }

    /**
     * Adiciona um novo entry no arquivo XML
     */
    public void adicionaEntry(String filename, String tipoDigest, String hexDigest) {
        // se o arquivo nao existir, cria um novo entry
        if (!this.entries.containsKey(filename)) {
            this.entries.put(filename, new HashMap<>());
        }
        // adiciona o novo digest
        this.entries.get(filename).put(tipoDigest, hexDigest);
    }


    /**
     * Atualiza o arquivo XML com os digests encontrados na pasta
     */
    public void atualizar() throws Exception {
        // https://mkyong.com/java/how-to-create-xml-file-in-java-dom/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // criando a tag raiz
        Element catalog = doc.createElement("CATALOG");
        doc.appendChild(catalog);

        // iterando sobre cada file entry
        for (String filename : this.entries.keySet()) {
            // criando a tag FILE_ENTRY
            Element fileEntry = doc.createElement("FILE_ENTRY");
            catalog.appendChild(fileEntry);

            // criando a tag FILE_NAME
            Element fileName = doc.createElement("FILE_NAME");
            fileName.appendChild(doc.createTextNode(filename));
            fileEntry.appendChild(fileName);

            // criando as tags DIGEST_ENTRY
            HashMap<String, String> digests = this.entries.get(filename);
            for (String tipoDigest : digests.keySet()) {
                // criando a tag DIGEST_ENTRY
                Element digestEntry = doc.createElement("DIGEST_ENTRY");
                fileEntry.appendChild(digestEntry);

                // criando a tag DIGEST_TYPE
                Element digestType = doc.createElement("DIGEST_TYPE");
                digestType.appendChild(doc.createTextNode(tipoDigest));
                digestEntry.appendChild(digestType);

                // criando a tag DIGEST_HEX
                Element digestHex = doc.createElement("DIGEST_HEX");
                digestHex.appendChild(doc.createTextNode(digests.get(tipoDigest)));
                digestEntry.appendChild(digestHex);
            }
        }

        // escrevendo o arquivo XML
        try (FileOutputStream output = new FileOutputStream(this.caminho)) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");    // pretty print
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        }
    }
}
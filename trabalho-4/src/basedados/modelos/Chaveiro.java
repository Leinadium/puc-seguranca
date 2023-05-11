package basedados.modelos;

public class Chaveiro {
    /** identificador da chave */
    public int kid;

    /** certificado do usuario (em bytes) */
    public String chavePublicaPem;

    /** chave privada do usuario (em bytes) */
    public byte[] chavePrivadaBytes;

}
